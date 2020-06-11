package iaf.ofek.hadracha.base_course.web_server.AirSituation;

import iaf.ofek.hadracha.base_course.web_server.Utilities.GeographicCalculations;
import iaf.ofek.hadracha.base_course.web_server.Utilities.Globals;
import iaf.ofek.hadracha.base_course.web_server.Utilities.RandomGenerators;
import iaf.ofek.hadracha.base_course.web_server.Data.Coordinates;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A simulator that manages airplanes and randomly advances them in position over time
 */
@Service
public class SimulativeAirSituationProvider implements AirSituationProvider {

    private static final double CHANCE_FOR_NUMBER_CHANGE = 0.005;
    private static final double CHANCE_FOR_AZIMUTH_CHANGE = 0.05;
    private static final int STEP_SIZE = 15;
    private static final int SIMULATION_INTERVAL_MILLIS = 100;
    private static final double LAT_MIN = 29.000;
    private static final double LAT_MAX = 36.000;
    private static final double LON_MIN = 32.000;
    private static final double LON_MAX = 46.500;
    private static final double AZIMUTH_STEP = STEP_SIZE / (2000.0 / SIMULATION_INTERVAL_MILLIS);
    private static final int MAX_NUMBER_OF_AIRPLANES = 80;
    private static final double MAX_BASE_VELOCITY = 70;
    private static final double MIN_BASE_VELOCITY = 40;
    private final double THRESHOLD_TO_ARRIVE_TO_DESTINATION = 500;

    // Scheduler to run advancement task repeatedly
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    // Object that acts as a lock for the updates
    private final Object lock = new Object();
    private final Random random = new Random();
    private final RandomGenerators randomGenerators;
    private final GeographicCalculations geographicCalculations;

    // The current air situation
    private List<Airplane> airplanes = new ArrayList<>();
    private int lastId = 0;


    public SimulativeAirSituationProvider(RandomGenerators randomGenerators, GeographicCalculations geographicCalculations) {
        this.randomGenerators = randomGenerators;
        this.geographicCalculations = geographicCalculations;

        for (int i = 0; i < MAX_NUMBER_OF_AIRPLANES; i++) {
            createAirplaneForSimulation();
        }

        executor.scheduleAtFixedRate(this::updateSituation, 0, SIMULATION_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    // all airplane kinds that can be used
    private List<AirplaneKind> airplaneKinds = AirplaneKind.LeafKinds();

    private void createAirplaneForSimulation() {
        AirplaneKind kind = airplaneKinds.get(random.nextInt(airplaneKinds.size()));
        Airplane airplane = new Airplane(kind, lastId++);
        airplane.coordinates=new Coordinates(randomGenerators.generateRandomDoubleInRange(LAT_MIN, LAT_MAX),
                randomGenerators.generateRandomDoubleInRangeWithNormalDistribution(LON_MIN, LON_MAX));
        airplane.setAzimuth(randomGenerators.generateRandomDoubleInRange(0,Globals.HALF_A_CIRCLE_IN_DEGREES * 2));
        airplane.velocity = randomGenerators.generateRandomDoubleInRange(MIN_BASE_VELOCITY, MAX_BASE_VELOCITY)*airplane.getAirplaneKind().getVelocityFactor();
        airplanes.add(airplane);
    }

    private void updateSituation() {
        try {
            synchronized (lock) {
                if (random.nextDouble() < CHANCE_FOR_NUMBER_CHANGE) { // chance to remove an airplane
                    int indexToRemove;
                    do {
                        indexToRemove = random.nextInt(airplanes.size());
                    } while (airplanes.get(indexToRemove).isAllocated());// don't remove allocated airplane
                    airplanes.remove(indexToRemove);
                }


                airplanes.forEach(airplane -> {
                    airplane.radialAcceleration = calculateNewRadialAcceleration(airplane);

                    airplane.setAzimuth(airplane.getAzimuth() + airplane.radialAcceleration);
                    final int SCALE = 100000;   // Scales the map
                    airplane.coordinates.lat += Math.sin(worldAzimuthToEuclidRadians(airplane.getAzimuth())) * airplane.velocity / SCALE;
                    airplane.coordinates.lon += Math.cos(worldAzimuthToEuclidRadians(airplane.getAzimuth())) * airplane.velocity / SCALE;
                    if (airplane.coordinates.lat < LAT_MIN || airplane.coordinates.lat > LAT_MAX ||
                            airplane.coordinates.lon < LON_MIN || airplane.coordinates.lon > LON_MAX)
                        airplane.setAzimuth(airplane.getAzimuth() + Globals.HALF_A_CIRCLE_IN_DEGREES);
                });

                if (random.nextDouble() < CHANCE_FOR_NUMBER_CHANGE) { // chance to add an airplane
                    createAirplaneForSimulation();
                }
            }
        }
        catch (Exception e){
            System.err.println("Error while updating air situation picture" + e.getMessage());
            e.printStackTrace();
        }
    }

    private double calculateNewRadialAcceleration(Airplane airplane) {
        if (airplane.isAllocated()) {
            Coordinates currLocation = airplane.coordinates;
            Coordinates headingTo = airplane.headingTo;

            if (arrivedToDestination(currLocation, headingTo)) {
                airplane.raiseArrivedAtDestinationEvent();
            }

            double azimuthToDestenation = geographicCalculations.azimuthBetween(currLocation, headingTo);
            double differnceOfAzimuth = Globals.HALF_A_CIRCLE_IN_DEGREES-geographicCalculations.normalizeAzimuth(azimuthToDestenation - airplane.getAzimuth());

            return (differnceOfAzimuth > 0 ? Math.min(AZIMUTH_STEP*10, differnceOfAzimuth/5) : Math.max(-AZIMUTH_STEP*10, differnceOfAzimuth/5))/2;

        }
        else {
            if (random.nextDouble() < CHANCE_FOR_AZIMUTH_CHANGE)       // chance for any change
                if (random.nextDouble() < CHANCE_FOR_AZIMUTH_CHANGE)   // chance for big change
                    return randomGenerators.generateRandomDoubleInRange(-AZIMUTH_STEP, AZIMUTH_STEP);
                else   // small gradual change, with a 66% chance that the size of the acceleration will be reduced
                    return randomGenerators.generateRandomDoubleInRange(0, 1.5 * airplane.radialAcceleration);
            return airplane.radialAcceleration;
        }
    }

    private boolean arrivedToDestination(Coordinates currLocation, Coordinates headingTo) {
        return geographicCalculations.distanceBetween(currLocation, headingTo) < THRESHOLD_TO_ARRIVE_TO_DESTINATION;
    }

    /**
     * Gets world azimuth - degrees in which 0 is up and increases clockwise and converts it to
     * radians in which 0 is right and increases counter clockwise.
     */
    private double worldAzimuthToEuclidRadians(double azimuth) {
        double inEuclidDegrees = -azimuth + (double)Globals.HALF_A_CIRCLE_IN_DEGREES / 2;
        return inEuclidDegrees * Math.PI / Globals.HALF_A_CIRCLE_IN_DEGREES;
    }

    @Override
    public List<Airplane> getAllAirplanes() {
        synchronized (lock){
            return new ArrayList<>(airplanes);
        }
    }
}
