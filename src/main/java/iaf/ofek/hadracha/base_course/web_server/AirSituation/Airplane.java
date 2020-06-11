package iaf.ofek.hadracha.base_course.web_server.AirSituation;

import iaf.ofek.hadracha.base_course.web_server.Data.Coordinates;
import iaf.ofek.hadracha.base_course.web_server.Data.Entity;
import iaf.ofek.hadracha.base_course.web_server.Utilities.Globals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Airplane implements Entity<Airplane> {
    private AirplaneKind airplaneKind;

    public int id;

    public Coordinates coordinates;
    public Coordinates headingTo;
    public String controllerClientId;

    /**
     * Azimuth in degrees. 0 indicates north, 90 indicates east
     */
    private double azimuth;
    /**
     * Velocity in KPH.
     */
    public double velocity;

    /**
     * The acceleration of change of azimuth
     */
    double radialAcceleration;

    private List<Consumer<Airplane>> arrivedAtDestinationCallbacks = new ArrayList<>(1);


    public Airplane(AirplaneKind airplaneKind, int id) {
        this.airplaneKind = airplaneKind;
        this.id = id;
    }

    public AirplaneKind getAirplaneKind() {
        return airplaneKind;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Azimuth in degrees. 0 indicates north, 90 indicates east
     */
    public double getAzimuth() {
        return azimuth;
    }

    /**
     * Sets the azimuth of the aircraft. The given azimuth will be normalized to [0..360]
     */
    public void setAzimuth(double azimuth) {
        while (azimuth < 0)
            azimuth += (Globals.HALF_A_CIRCLE_IN_DEGREES * 2);
        this.azimuth = azimuth % (Globals.HALF_A_CIRCLE_IN_DEGREES * 2);
    }

    @Override
    public Airplane clone() {
        try {
            return (Airplane) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Not possible - Entity implements Cloneable");
        }
    }

    public void flyTo(Coordinates headTo, String controllerClientId) {
        headingTo = headTo;
        this.controllerClientId = controllerClientId;
    }

    public boolean isAllocated() {
        return controllerClientId != null;
    }

    public void unAllocate() {
        headingTo = null;
        controllerClientId = null;
        arrivedAtDestinationCallbacks.clear();
    }

    public void onArrivedAtDestination(Consumer<Airplane> callback) {
        arrivedAtDestinationCallbacks.add(callback);
    }

    void raiseArrivedAtDestinationEvent() {
        //why new array list? because callback might call unAllocate which modifies this collection
        new ArrayList<>(arrivedAtDestinationCallbacks).forEach(airplaneConsumer -> airplaneConsumer.accept(this));
    }
}
