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
@Schema(description = "Kubernetes Secret representation containing sensitive data")
public class KubernetesSecret {

    @Schema(description = "Name of the Secret", example = "app-secret")
    private String name;

    @Schema(description = "Key-value pairs containing secret data (base64 encoded in Kubernetes)",
            example = "{\"database.password\": \"cGFzc3dvcmQxMjM=\", \"api.key\": \"YWJjZGVmZ2hpams=\"}")
    private Map<String, String> data;
}
