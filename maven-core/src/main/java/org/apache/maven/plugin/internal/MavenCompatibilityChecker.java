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

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.DependencyGraphTransformationContext;
import org.eclipse.aether.collection.DependencyGraphTransformer;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.version.VersionRange;

/**
 * Ensure that Maven core dependencies fit within the versionRange
 * 
 * @since 4.0.0
 */
class MavenCompatibilityChecker implements DependencyGraphTransformer
{
    private final VersionRange versionRange;
    
    MavenCompatibilityChecker( VersionRange versionRange )
    {
        this.versionRange = versionRange;
    }

    @Override
    public DependencyNode transformGraph( DependencyNode node, DependencyGraphTransformationContext context )
        throws RepositoryException
    {
        try 
        {
            validateNode( node );
        }
        catch ( IncompatibleDependencyException e )
        {
            throw new RepositoryException( 
                       String.format( "%s depends on %s, which does not match the required Maven versionrange of %s",
                                      node.getArtifact(), e.getNode().getArtifact(), versionRange ) );
        }

        return node;
    }
    
    private void validateNode( DependencyNode node )
        throws IncompatibleDependencyException
    {
        if ( isCoreArtifact( node.getArtifact() ) && !versionRange.containsVersion( node.getVersion() ) ) 
        {
            throw new IncompatibleDependencyException( node );
        }
        
        for ( DependencyNode child : node.getChildren() )
        {
            validateNode( child );
        }
    }
    
    private static boolean isCoreArtifact( Artifact artifact )
    {
        return artifact.getArtifactId().startsWith( "maven-" )
            && artifact.getGroupId().equals( "org.apache.maven" );
    }
}
