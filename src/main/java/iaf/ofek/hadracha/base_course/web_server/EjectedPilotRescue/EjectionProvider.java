package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import iaf.ofek.hadracha.base_course.web_server.Data.CrudDataBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EjectionProvider implements IEjectionProvider {
    CrudDataBase dataBase;

    public EjectionProvider(@Autowired CrudDataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public List<EjectedPilotInfo> getAllEjections() {
        return dataBase.getAllOfType(EjectedPilotInfo.class);
    }

    @Override
    public void setRescuer(int ejectionId, String rescuerId) {
        EjectedPilotInfo ejectedPilotInfo = dataBase.getByID(ejectionId, EjectedPilotInfo.class);
        // Has to check if ejectionId is a valid Id or not, means ejection is real or null!!!!!!!!!
        ejectedPilotInfo.setRescuedBy(rescuerId);
        dataBase.update(ejectedPilotInfo);
    }

}
