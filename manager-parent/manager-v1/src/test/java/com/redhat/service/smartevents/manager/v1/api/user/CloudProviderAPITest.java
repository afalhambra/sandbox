package com.redhat.service.smartevents.manager.v1.api.user;

import org.junit.jupiter.api.Test;

import com.redhat.service.smartevents.infra.v1.api.V1APIConstants;
import com.redhat.service.smartevents.manager.core.api.models.responses.CloudRegionListResponse;
import com.redhat.service.smartevents.manager.core.api.models.responses.CloudRegionResponse;
import com.redhat.service.smartevents.manager.v1.api.models.responses.CloudProviderListResponse;
import com.redhat.service.smartevents.manager.v1.api.models.responses.CloudProviderResponse;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class CloudProviderAPITest {

    @Test
    public void listCloudProviders() {
        CloudProviderListResponse cloudProviders = given()
                .basePath(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH)
                .contentType(ContentType.JSON)
                .when()
                .get()
                .as(CloudProviderListResponse.class);

        assertThat(cloudProviders.getItems().size()).isEqualTo(1);
        assertThat(cloudProviders.getPage()).isZero();
        assertThat(cloudProviders.getSize()).isEqualTo(1);
        assertThat(cloudProviders.getTotal()).isEqualTo(1);

        CloudProviderResponse cloudProviderResponse = cloudProviders.getItems().get(0);
        assertThat(cloudProviderResponse.getKind()).isEqualTo("CloudProvider");
        assertThat(cloudProviderResponse.getId()).isEqualTo("aws");
        assertThat(cloudProviderResponse.getName()).isEqualTo("aws");
        assertThat(cloudProviderResponse.getDisplayName()).isEqualTo("Amazon Web Services");
        assertThat(cloudProviderResponse.isEnabled()).isTrue();
        assertThat(cloudProviderResponse.getHref()).isEqualTo(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/aws");
    }

    @Test
    public void getCloudProvider() {
        CloudProviderResponse cloudProvider = given()
                .basePath(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/aws")
                .contentType(ContentType.JSON)
                .when()
                .get()
                .as(CloudProviderResponse.class);

        assertThat(cloudProvider.getKind()).isEqualTo("CloudProvider");
        assertThat(cloudProvider.getId()).isEqualTo("aws");
        assertThat(cloudProvider.getName()).isEqualTo("aws");
        assertThat(cloudProvider.getDisplayName()).isEqualTo("Amazon Web Services");
        assertThat(cloudProvider.isEnabled()).isTrue();
        assertThat(cloudProvider.getHref()).isEqualTo(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/aws");
    }

    @Test
    public void listCloudProviderRegions() {
        CloudRegionListResponse cloudRegions = given()
                .basePath(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/aws/regions")
                .contentType(ContentType.JSON)
                .when()
                .get()
                .as(CloudRegionListResponse.class);

        assertThat(cloudRegions.getItems().size()).isEqualTo(1);
        assertThat(cloudRegions.getPage()).isZero();
        assertThat(cloudRegions.getSize()).isEqualTo(1);
        assertThat(cloudRegions.getTotal()).isEqualTo(1);

        CloudRegionResponse cloudRegion = cloudRegions.getItems().get(0);
        assertThat(cloudRegion.getKind()).isEqualTo("CloudRegion");
        assertThat(cloudRegion.getName()).isEqualTo("us-east-1");
        assertThat(cloudRegion.getDisplayName()).isEqualTo("US East, N. Virginia");
        assertThat(cloudRegion.isEnabled()).isTrue();
    }

    @Test
    public void listCloudProviderRegions_unknownCloudProvider() {
        int fourOhFour = given()
                .basePath(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/azure/regions")
                .contentType(ContentType.JSON)
                .when()
                .get()
                .andReturn()
                .statusCode();
        assertThat(fourOhFour).isEqualTo(404);
    }

    @Test
    public void getCloudProvider_unknownCloudProvider() {
        int fourOhFour = given()
                .basePath(V1APIConstants.V1_CLOUD_PROVIDERS_BASE_PATH + "/foobar")
                .contentType(ContentType.JSON)
                .when()
                .get()
                .andReturn()
                .statusCode();
        assertThat(fourOhFour).isEqualTo(404);
    }
}
