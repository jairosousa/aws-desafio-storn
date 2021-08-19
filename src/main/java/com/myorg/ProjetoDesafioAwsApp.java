package com.myorg;

import software.amazon.awscdk.core.App;

public class ProjetoDesafioAwsApp {
    public static void main(final String[] args) {
        App app = new App();

        VpcStack vpcStack = new VpcStack(app, "Vpcdesafio");

        ClusterStack clusterStack = new ClusterStack(app, "ClusterDesafio", vpcStack.getVpc());
        clusterStack.addDependency(vpcStack);

        ServiceDesafioStack desafioStack = new ServiceDesafioStack(app, "ServiceDesafio", clusterStack.getCluster());
        desafioStack.addDependency(clusterStack);

        app.synth();
    }
}
