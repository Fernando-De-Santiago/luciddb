/*
// Licensed to DynamoBI Corporation (DynamoBI) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  DynamoBI licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at

//   http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
*/
package net.sf.farrago.util;

import org.eigenbase.util.*;


/**
 * FarragoCompoundAllocation represents a collection of FarragoAllocations which
 * share a common lifecycle. It guarantees that allocations are closed in the
 * reverse order in which they were added.
 *
 * <p>REVIEW: SWZ: 2/22/2006: New code should use CompoundClosableAllocation
 * directly when possible. Eventually remove this class and replace all usages
 * with CompoundClosableAllocation.
 *
 * @author John V. Sichi
 * @version $Id$
 */
public class FarragoCompoundAllocation
    extends CompoundClosableAllocation
    implements FarragoAllocationOwner
{
    //~ Constructors -----------------------------------------------------------

    public FarragoCompoundAllocation()
    {
        super();
    }
}

// End FarragoCompoundAllocation.java
