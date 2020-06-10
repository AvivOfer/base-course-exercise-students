package iaf.ofek.hadracha.base_course.web_server.EjectedPilotRescue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ejectedPilotRescue")
public class EjectionRestController {
    IEjectionProvider ejectionProvider;

    public EjectionRestController(@Autowired IEjectionProvider ejectionProvider) {
        this.ejectionProvider = ejectionProvider;
    }

    @GetMapping("/infos")
    public List<EjectedPilotInfo> showAllEjections() {
        return ejectionProvider.getAllEjections();
    }

    @GetMapping("/takeResponsibility")
    public List<EjectedPilotInfo> takeResponsibility(int ejectionId, @CookieValue(value = "client-id") String clientId) {
        ejectionProvider.allocateAirplanes(ejectionId, clientId);
        ejectionProvider.setRescuer(ejectionId, clientId);
        return ejectionProvider.getAllEjections();
    }
}
