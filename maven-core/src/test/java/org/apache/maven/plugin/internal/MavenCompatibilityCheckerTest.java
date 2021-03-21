package org.apache.maven.plugin.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.VersionScheme;
import org.junit.jupiter.api.Test;

class MavenCompatibilityCheckerTest
{
    private final VersionScheme versionScheme = new GenericVersionScheme();

    @Test
    void compatible() throws Exception
    {
        MavenCompatibilityChecker checker =
            new MavenCompatibilityChecker( versionScheme.parseVersionRange( "[3.0,)" ) );
        
        Artifact pluginArtifact = new DefaultArtifact( "o.a.m.p:plugin:1.0" );
        Dependency plugin = new Dependency( pluginArtifact, "compile" );
        DefaultDependencyNode node = new DefaultDependencyNode( plugin );
        node.setVersion( versionScheme.parseVersion( "1.0" ) );

        Artifact coreArtifact = new DefaultArtifact( "org.apache.maven:maven-core:3.0" );
        Dependency core = new Dependency( coreArtifact, "compile" );
        DefaultDependencyNode coreNode = new DefaultDependencyNode( core );
        coreNode.setVersion( versionScheme.parseVersion( "3.0" ) );
        
        node.setChildren(Collections.singletonList( coreNode ) );
        
        assertThat( checker.transformGraph( node, null ), is(node));
    }

    @Test
    void incompatible() throws Exception
    {
        MavenCompatibilityChecker checker =
                        new MavenCompatibilityChecker( versionScheme.parseVersionRange( "[3.0,)" ) );
                    
        Artifact pluginArtifact = new DefaultArtifact( "o.a.m.p:plugin:1.0" );
        Dependency plugin = new Dependency( pluginArtifact, "compile" );
        DefaultDependencyNode node = new DefaultDependencyNode( plugin );
        node.setVersion( versionScheme.parseVersion( "1.0" ) );

        Artifact coreArtifact = new DefaultArtifact( "org.apache.maven:maven-core:2.0" );
        Dependency core = new Dependency( coreArtifact, "compile" );
        DefaultDependencyNode coreNode = new DefaultDependencyNode( core );
        coreNode.setVersion( versionScheme.parseVersion( "2.0" ) );
        
        node.setChildren(Collections.singletonList( coreNode ) );
        
        RepositoryException exception = assertThrows( RepositoryException.class, () -> checker.transformGraph( node, null ) );
        assertThat( exception.getMessage(), is( "o.a.m.p:plugin:jar:1.0 depends on org.apache.maven:maven-core:jar:2.0, "
            + "which does not match the required Maven versionrange of [3.0,)" ) );
    }

}
