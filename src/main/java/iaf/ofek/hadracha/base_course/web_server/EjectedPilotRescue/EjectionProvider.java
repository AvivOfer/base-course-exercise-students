package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.Data.CrudDataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EjectionProvider implements IEjectionProvider {
    private CrudDataBase dataBase;
    private AirplanesAllocationManager airplanesAllocationManager;

    public EjectionProvider(@Autowired CrudDataBase dataBase,
                            @Autowired AirplanesAllocationManager airplanesAllocationManager) {
        this.dataBase = dataBase;
        this.airplanesAllocationManager = airplanesAllocationManager;
    }

    @Override
    public List<EjectedPilotInfo> getAllEjections() {
        return dataBase.getAllOfType(EjectedPilotInfo.class);
    }

    @Override
    public void setRescuer(int ejectionId, String rescuerId) {
        EjectedPilotInfo ejectedPilotInfo = dataBase.getByID(ejectionId, EjectedPilotInfo.class);
        // Has to check if ejectionId is a valid Id or not, means ejection is real or null!!!!!!!!!
        if (ejectedPilotInfo.getRescuedBy() == null) {
            ejectedPilotInfo.setRescuedBy(rescuerId);
            dataBase.update(ejectedPilotInfo);
        }
    }

    public void allocateAirplanes(int ejectionId, String rescuerId) {
        EjectedPilotInfo ejectedPilotInfo = dataBase.getByID(ejectionId, EjectedPilotInfo.class);
        if (ejectedPilotInfo.getRescuedBy() == null) {
            airplanesAllocationManager.allocateAirplanesForEjection(ejectedPilotInfo, rescuerId);
        }
    }

}
