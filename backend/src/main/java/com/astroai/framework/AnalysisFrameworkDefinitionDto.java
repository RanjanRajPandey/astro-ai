package com.astroai.framework;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisFrameworkDefinitionDto(
        @JsonAlias("category_code") String categoryCode,
        @JsonAlias("title") String title,
        @JsonAlias("sanskrit_title") String sanskritTitle,
        @JsonAlias("description") String description,
        @JsonAlias("primary_houses") List<Integer> primaryHouses,
        @JsonAlias("secondary_houses") List<Integer> secondaryHouses,
        @JsonAlias("required_vargas") List<String> requiredVargas,
        @JsonAlias("naisargika_karakas") List<String> naisargikaKarakas,
        @JsonAlias("special_lagnas") List<String> specialLagnas,
        @JsonAlias("key_yogas_to_check") List<String> keyYogasToCheck,
        @JsonAlias("checklist_rules") List<FrameworkChecklistRuleDto> checklistRules
) {
}
