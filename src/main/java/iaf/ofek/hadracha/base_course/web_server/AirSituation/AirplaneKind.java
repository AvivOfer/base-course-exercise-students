package iaf.ofek.hadracha.base_course.web_server.AirSituation;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AirplaneKind{
    Krav(null, "קרב"), F15(Krav, "F15"), F16(Krav, "F16"),
    Katmam(null, "כטמ\"מ"), Zik(Katmam, "זיק"), Shoval(Katmam, "שובל"),
    Helicopter(null, "מסוק"), Maskar(Helicopter, "מסק\"ר"), Masaar(Helicopter, "מסע\"ר "),
    Yanshoof(Masaar, "ינשוף"), Saraf(Maskar, "שרף");


    private AirplaneKind parent;
    private String displayName;

    AirplaneKind(AirplaneKind parent, String displayName) {
        this.parent = parent;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // serves as a cache for LeafKinds()
    private static List<AirplaneKind> leaves;

    /**
     * Returns all the airplane kinds that are not parents of any other kind.
     * @return A list of airplane kinds that are leaves.
     */
    public static List<AirplaneKind> LeafKinds(){
        if (leaves == null) {
            Map<AirplaneKind, Set<AirplaneKind>> children = new HashMap<>();
            Arrays.stream(values()).forEach(airplaneKind -> {
                if (!children.containsKey(airplaneKind))
                    children.put(airplaneKind, new HashSet<>());
                if (airplaneKind.parent != null) {
                    if (!children.containsKey(airplaneKind.parent))
                        children.put(airplaneKind.parent, new HashSet<>());
                    children.get(airplaneKind.parent).add(airplaneKind);
                }
            });
            leaves = getKeysWithEmptySetAsValue(children).collect(Collectors.toList());
        }

        return leaves;
    }

    @NotNull
    private static Stream<AirplaneKind> getKeysWithEmptySetAsValue(Map<AirplaneKind, Set<AirplaneKind>> children) {
        return children.entrySet().stream().filter(entry -> entry.getValue().isEmpty()).map(Map.Entry::getKey);
    }
}