/*
// $Id$
// Fennel is a library of data storage and processing components.
// Copyright (C) 2004-2005 LucidEra, Inc.
// Copyright (C) 2005-2005 The Eigenbase Project
// Portions Copyright (C) 2004-2005 John V. Sichi
//
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your option)
// any later version approved by The Eigenbase Project.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#include "fennel/common/CommonPreamble.h"
#include "fennel/test/ExecStreamTestBase.h"
#include "fennel/lucidera/sorter/ExternalSortExecStream.h"
#include "fennel/tuple/StandardTypeDescriptor.h"
#include "fennel/exec/MockProducerExecStream.h"
#include "fennel/exec/ExecStreamEmbryo.h"
#include "fennel/cache/Cache.h"

#include <boost/test/test_tools.hpp>

using namespace fennel;

class ExternalSortExecStreamTest : public ExecStreamTestBase
{
    void testImpl(
        uint nRows,
        SharedMockProducerExecStreamGenerator pGenerator,
        MockProducerExecStreamGenerator &verifier);
    
public:
    explicit ExternalSortExecStreamTest()
    {
        FENNEL_UNIT_TEST_CASE(ExternalSortExecStreamTest,testPresortedInMem);
        FENNEL_UNIT_TEST_CASE(ExternalSortExecStreamTest,testPresortedExternal);
        FENNEL_UNIT_TEST_CASE(ExternalSortExecStreamTest,testRandomInMem);
        FENNEL_UNIT_TEST_CASE(ExternalSortExecStreamTest,testRandomExternal);
    }

    void testPresortedInMem();
    void testPresortedExternal();
    void testRandomInMem();
    void testRandomExternal();
};

void ExternalSortExecStreamTest::testRandomInMem()
{
    SharedMockProducerExecStreamGenerator pGenerator(
        new PermutationGenerator(100));
    RampExecStreamGenerator verifier;
    testImpl(100, pGenerator, verifier);
}

void ExternalSortExecStreamTest::testRandomExternal()
{
    SharedMockProducerExecStreamGenerator pGenerator(
        new PermutationGenerator(10000));
    RampExecStreamGenerator verifier;
    testImpl(10000, pGenerator, verifier);
}

void ExternalSortExecStreamTest::testPresortedInMem()
{
    SharedMockProducerExecStreamGenerator pGenerator(
        new RampExecStreamGenerator());
    testImpl(100, pGenerator, *pGenerator);
}

void ExternalSortExecStreamTest::testPresortedExternal()
{
    SharedMockProducerExecStreamGenerator pGenerator(
        new RampExecStreamGenerator());
    testImpl(10000, pGenerator, *pGenerator);
}

void ExternalSortExecStreamTest::testImpl(
    uint nRows,
    SharedMockProducerExecStreamGenerator pGenerator,
    MockProducerExecStreamGenerator &verifier)
{
    StandardTypeDescriptorFactory stdTypeFactory;
    TupleAttributeDescriptor attrDesc(
        stdTypeFactory.newDataType(STANDARD_TYPE_INT_64));
    
    MockProducerExecStreamParams mockParams;
    mockParams.outputTupleDesc.push_back(attrDesc);
    mockParams.nRows = nRows;
    mockParams.pGenerator = pGenerator;

    ExecStreamEmbryo mockStreamEmbryo;
    mockStreamEmbryo.init(new MockProducerExecStream(),mockParams);
    mockStreamEmbryo.getStream()->setName("MockProducerExecStream");

    ExternalSortExecStreamParams sortParams;
    sortParams.outputTupleDesc.push_back(attrDesc);
    sortParams.distinctness = DUP_ALLOW;
    pRandomSegment = pSegmentFactory->newRandomAllocationSegment(
        pLinearSegment, true);
    pLinearSegment.reset();
    sortParams.pTempSegment = pRandomSegment;
    sortParams.pCacheAccessor = pCache;
    sortParams.scratchAccessor =
        pSegmentFactory->newScratchSegment(pCache, 10);
    sortParams.keyProj.push_back(0);
    sortParams.storeFinalRun = false;
    
    ExecStreamEmbryo sortStreamEmbryo;
    sortStreamEmbryo.init(
        ExternalSortExecStream::newExternalSortExecStream(),sortParams);
    sortStreamEmbryo.getStream()->setName("ExternalSortExecStream");
    
    SharedExecStream pOutputStream = prepareTransformGraph(
        mockStreamEmbryo, sortStreamEmbryo);

    verifyOutput(
        *pOutputStream,
        mockParams.nRows,
        verifier);
}

FENNEL_UNIT_TEST_SUITE(ExternalSortExecStreamTest);

// End ExternalSortExecStreamTest.cpp