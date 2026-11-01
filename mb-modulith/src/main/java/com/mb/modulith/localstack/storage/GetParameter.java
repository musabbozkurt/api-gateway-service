package com.mb.modulith.localstack.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.SsmException;

@Slf4j
public class GetParameter {

    static void main(String[] args) {
        final String USAGE = """
                Usage:
                    GetParameter <paraName>
                
                Where:
                    paraName - the name of the parameter
                """;

        if (args.length < 1) {
            log.info("{}", USAGE);
            System.exit(1);
        }

        // Get args
        String paraName = args[0];

        Region region = Region.US_EAST_1;
        try (SsmClient ssmClient = SsmClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(region)
                .build()) {
            GetParameterRequest parameterRequest = GetParameterRequest.builder()
                    .name(paraName)
                    .build();

            GetParameterResponse parameterResponse = ssmClient.getParameter(parameterRequest);
            log.info("The parameter value is {}", parameterResponse.parameter().value());

        } catch (SsmException e) {
            log.error("Failed to get the parameter: {}", e.getMessage());
            System.exit(1);
        }
    }
}
