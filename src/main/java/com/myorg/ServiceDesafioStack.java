package com.myorg;

import software.amazon.awscdk.core.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;

/**
 * @Autor Jairo Nascimento
 * @Created 19/08/2021 - 17:20
 */
public class ServiceDesafioStack extends Stack {

    public ServiceDesafioStack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public ServiceDesafioStack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);

        // Criar service AplicatioLoadBalance tipo Fargate,
        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALBS01")
                .serviceName("serviceDesafio01")
                .cluster(cluster)
                .cpu(512)
                .memoryLimitMiB(1024)
                .desiredCount(2) //inicia com 2 duas instancias
                .listenerPort(8080)
                .taskImageOptions(// aqui cria a tarefa, especificar tudo vai ser criado
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project_desafio")
                                .image(ContainerImage.fromRegistry("jnsousa/desafio:v1")) // aqui efine a imagem deve estar no DockerHub
                                .containerPort(8080)
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this, "ServiceDesafioLogGroup")
                                                .logGroupName("ServiceDesafio")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("ServiceDesafio")
                                        .build()))
                                .build()
                )
                .publicLoadBalancer(true)
                .build();

        // Criação do Target Groups
        service01.getTargetGroup().configureHealthCheck(HealthCheck.builder()
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        // Criação auto-scaling
        ScalableTaskCount scalableTaskCount = service01.getService().autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        // Regra para alto-scaling => se o consumo médio da cpu alcançar 50% por 60 segundos ele cria novas instancias no limite de 4 instancias,
        // Se atingir comumo de cpu abaixo de 50% ele destroy uma instancia.
        scalableTaskCount.scaleOnCpuUtilization("ServiceDesafio01AutoScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(50)
                .scaleInCooldown(Duration.seconds(60))
                .scaleOutCooldown(Duration.seconds(60))
                .build());
    }
}
