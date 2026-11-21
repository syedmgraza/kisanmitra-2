from pathlib import Path
from typing import Any
import hmac
import os

import joblib
import numpy as np
import pandas as pd
from fastapi import Depends, FastAPI, Header, HTTPException, Query
from pydantic import BaseModel, Field

BASE = Path.cwd()
app = FastAPI(title="KisanMitra ML Service", version="1.0.0")
ML_SERVICE_API_KEY = os.getenv("ML_SERVICE_API_KEY", "")


def verify_service_key(x_ml_service_key: str | None = Header(default=None)) -> None:
    if ML_SERVICE_API_KEY and not hmac.compare_digest(x_ml_service_key or "", ML_SERVICE_API_KEY):
        raise HTTPException(status_code=401, detail="Invalid service credentials.")


class RecommendationRequest(BaseModel):
    state: str = Field(min_length=1)
    district: str = Field(min_length=1)
    season: str = Field(min_length=1)
    land_size: float = Field(gt=0, le=10000)


class YieldRequest(RecommendationRequest):
    crop: str = Field(min_length=1)
    rainfall: float = Field(ge=0, le=10000)
    fertilizer_choice: str = "Recommended"


@app.on_event("startup")
def load_assets() -> None:
    """Load models/data once per process; never unpickle user supplied files."""
    global rec_model, yield_model, history, yield_data, crop_reference, weather_data, rain_normal
    try:
        rec_model = joblib.load(BASE / "final_model.pkl")
        yield_model = joblib.load(BASE / "models" / "yield_model.pkl")
        history = pd.read_csv(BASE / "final_data.csv")
        yield_data = pd.read_csv(BASE / "data" / "clean_yield_data.csv")
        crop_reference = pd.read_csv(BASE / "crop_reference.csv").set_index("Crop")
        weather_data = pd.read_csv(BASE / "state_weather.csv")
        rain_normal = pd.read_csv(BASE / "data" / "rain_normal.csv").set_index("State")["Rain_normal"].to_dict()
    except Exception as exc:
        raise RuntimeError(f"Unable to load KisanMitra model assets from {BASE}: {exc}") from exc


@app.get("/health")
def health() -> dict[str, str]:
    ready = "rec_model" in globals() and "yield_model" in globals()
    return {"status": "ok" if ready else "starting", "models": "loaded" if ready else "loading"}


def _norm(value: str) -> str:
    value = value.strip().lower()
    aliases = {"andaman and nicobar islands": "andaman and nicobar island", "laddak": "ladakh", "laddakh": "ladakh",
               "the dadra and nagar haveli": "dadra and nagar haveli"}
    return aliases.get(value, value)


def _season_rows(frame: pd.DataFrame, state: str, district: str, season: str) -> pd.DataFrame:
    return frame[(frame.State.map(_norm) == _norm(state)) &
                 (frame.District.str.casefold() == district.casefold()) &
                 (frame.Season.str.casefold() == season.casefold())]


@app.post("/recommend")
def recommend(data: RecommendationRequest, _: None = Depends(verify_service_key)) -> dict[str, Any]:
    subset = _season_rows(history, data.state, data.district, data.season)
    if subset.empty:
        raise HTTPException(404, "No historical crop records match this state, district, and season.")
    groups = subset.groupby("Crop")
    quantiles = history.groupby("Crop").Yield.quantile([.30, .60]).unstack()
    next_year = int(history.Crop_Year.max()) + 1
    categories = {key: sorted(history[key].dropna().unique().tolist()) for key in ["State", "District", "Crop", "Season"]}
    rows = []
    for crop, group in groups:
        group = group.sort_values("Crop_Year")
        features = pd.DataFrame([{"State": group.State.iloc[-1], "District": group.District.iloc[-1], "Crop": crop, "Season": group.Season.iloc[-1],
                                  "Crop_Year": next_year, "Prev_Yield": float(group.Yield.iloc[-1]),
                                  "Prev3_Avg": float(group.Yield.tail(3).mean())}])
        for key, values in categories.items():
            features[key] = pd.Categorical(features[key], categories=values)
        prediction = max(0.0, float(rec_model.predict(features)[0]))
        lo, hi = quantiles.loc[crop, 0.30], quantiles.loc[crop, 0.60]
        performance = "Low" if prediction <= lo else ("Medium" if prediction <= hi else "High")
        try:
            price = float(crop_reference.loc[crop, "Price"])
            cost = float(crop_reference.loc[crop, "Cost"])
            effort = str(crop_reference.loc[crop, "Effort"])
            profit = round(prediction * 10 * price - cost)
        except (KeyError, TypeError, ValueError):
            price, cost, effort, profit = None, None, "Varies", None
        rows.append({"crop": crop, "predicted_yield_t_ha": round(prediction, 2), "performance": performance,
                     "effort": effort, "estimated_profit_rs_ha": profit, "land_size_hectares": data.land_size,
                     "estimated_production_tonnes": round(prediction * data.land_size, 2),
                     "confidence": "Low" if crop in {"Cardamom", "Black Pepper", "Cashewnut", "Niger Seed", "Castor Seed", "Sesamum", "Tobacco", "Safflower", "Dry Chillies"} else "Standard"})
    rows.sort(key=lambda row: (row["estimated_profit_rs_ha"] is None, -(row["estimated_profit_rs_ha"] or 0)))
    return {"state": data.state, "district": data.district, "season": data.season, "recommendations": rows}


@app.post("/predict-yield")
def predict_yield(data: YieldRequest, _: None = Depends(verify_service_key)) -> dict[str, Any]:
    record = _season_rows(yield_data, data.state, data.district, data.season)
    record = record[record.Crop.str.casefold() == data.crop.casefold()]
    if record.empty:
        raise HTTPException(404, "The selected crop does not have training records for this location and season.")
    features = pd.DataFrame([{"State": record.State.iloc[0], "District": record.District.iloc[0],
                              "Season": record.Season.iloc[0], "Crop": record.Crop.iloc[0], "Rainfall": data.rainfall}])
    try:
        base_q_ha = max(0.0, float(np.expm1(yield_model.predict(features)[0])))
    except Exception as exc:
        raise HTTPException(422, f"The model could not process this crop and location: {exc}") from exc
    rain_factor = 0.85 if data.fertilizer_choice.casefold() == "kam (below normal)" else 1.0
    fert_factor = 0.85 if data.fertilizer_choice.casefold() in {"less than recommended", "kam (less than recommended)"} else 1.0
    adjusted = base_q_ha * rain_factor * fert_factor
    return {"crop": data.crop, "season": data.season, "yield_quintals_per_hectare": round(adjusted, 2),
            "total_production_quintals": round(adjusted * data.land_size, 2),
            "total_production_tonnes": round(adjusted * data.land_size / 10, 2),
            "land_size_hectares": data.land_size, "rainfall_mm": data.rainfall,
            "guidance": "Estimate based on historical model data; actual results depend on field conditions."}


@app.get("/weather")
def weather(state: str = Query(min_length=1), season: str = Query(min_length=1),
            _: None = Depends(verify_service_key)) -> dict[str, Any]:
    rows = weather_data[(weather_data.State.map(_norm) == _norm(state)) &
                        (weather_data.Season.str.casefold() == season.casefold())]
    if rows.empty:
        raise HTTPException(404, "No seasonal weather summary is available for this state.")
    row = rows.iloc[0]
    return {"state": state, "season": season, "temperature_c": float(row.get("Temp", row.get("Temperature", 0))),
            "rainfall_mm": float(row.get("Rain", row.get("Rainfall", 0))),
            "humidity_percent": float(row.get("Humidity", row.get("Hum", 0))),
            "rainfall_normal_mm": float(rain_normal.get(row.State, 800)), "source": "Historical state and season averages"}
