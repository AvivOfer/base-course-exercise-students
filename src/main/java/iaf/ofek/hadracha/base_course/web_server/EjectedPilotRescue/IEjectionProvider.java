package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import java.util.List;

public interface IEjectionProvider {
    List<EjectedPilotInfo> getAllEjections();
    void setRescuer(int ejectionId, String rescuerId);
}
