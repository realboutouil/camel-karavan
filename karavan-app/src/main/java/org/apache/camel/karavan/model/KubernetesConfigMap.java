package org.apache.camel.karavan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Kubernetes ConfigMap representation containing configuration data")
public class KubernetesConfigMap {

    @Schema(description = "Name of the ConfigMap", example = "app-config")
    private String name;

    @Schema(description = "Key-value pairs containing configuration data",
            example = "{\"database.url\": \"jdbc:postgresql://localhost:5432/db\", \"app.name\": \"karavan\"}")
    private Map<String, String> data;
}
