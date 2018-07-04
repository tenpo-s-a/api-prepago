package cl.multicaja.prepaid.model.v10;

import cl.multicaja.core.model.BaseModel;

public class SimulationTopupGroup10 extends BaseModel {

  private SimulationTopup10 simulationTopupWeb;
  private SimulationTopup10 simulationTopupPOS;


  public SimulationTopup10 getSimulationTopupWeb() {
    return simulationTopupWeb;
  }

  public void setSimulationTopupWeb(SimulationTopup10 simulationTopupWeb) {
    this.simulationTopupWeb = simulationTopupWeb;
  }

  public SimulationTopup10 getSimulationTopupPOS() {
    return simulationTopupPOS;
  }

  public void setSimulationTopupPOS(SimulationTopup10 simulationTopupPOS) {
    this.simulationTopupPOS = simulationTopupPOS;
  }
}
