/*
// $Id$
// Farrago is an extensible data management system.
// Copyright (C) 2005-2005 The Eigenbase Project
// Copyright (C) 2005-2005 Disruptive Tech
// Copyright (C) 2005-2005 LucidEra, Inc.
// Portions Copyright (C) 2003-2005 John V. Sichi
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
package net.sf.farrago.namespace.impl;

import java.util.*;

import net.sf.farrago.query.*;

import org.eigenbase.rel.*;
import org.eigenbase.relopt.*;
import org.eigenbase.reltype.*;
import org.eigenbase.rex.RexInputRef;
import org.eigenbase.rex.RexNode;

/**
 * MedAbstractFennelProjectionRule is a base class for implementing
 * projection rules on different storage mechanisms
 *
 * @author Zelaine Fong
 * @version $Id$
 */
public abstract class MedAbstractFennelProjectionRule extends RelOptRule
{
    private String[] fieldNames;
    
    protected ProjectRel origProject;

    protected Integer [] projectedColumns;
    
    protected int numProjectedCols;

    /**
     * Creates a new MedAbstractFennelProjectionRule object.
     * 
     * @param operand root operand to pass to RelOptRule constructor
     */
    public MedAbstractFennelProjectionRule(RelOptRuleOperand operand)
    {
        super(operand);
    }
    
    // implement RelOptRule
    public CallingConvention getOutConvention()
    {
        return FennelRel.FENNEL_EXEC_CONVENTION;
    }
    
    public abstract void onMatch(RelOptRuleCall call);
    
    /**
     * Creates projection list for scan
     *
     * @note sets member variable numProjectedCols to > 0 if projection
     * rule can be applied
     *
     * @return true if columns in projection list need to be renamed
     */
    public boolean createProjectionList(FennelRel origScan)
    {
        // REVIEW:  what about AnonFields?
        // TODO:  rather than failing, split into parts that can be
        // pushed down and parts that can't
        int n = origProject.getChildExps().length;
        projectedColumns = new Integer[n];
        RelDataType rowType = origScan.getRowType();
        RelDataType projType = origProject.getRowType();
        RelDataTypeField [] projFields = projType.getFields();
        fieldNames = new String[n];
        boolean needRename = false;
        for (int i = 0; i < n; ++i) {
            RexNode exp = origProject.getChildExps()[i];
            if (!(exp instanceof RexInputRef)) {
                // rule does not apply
                numProjectedCols = 0;
                return false;
            }
            RexInputRef fieldAccess = (RexInputRef) exp;
            String projFieldName = projFields[i].getName();
            fieldNames[i] = projFieldName;
            String origFieldName =
                rowType.getFields()[fieldAccess.getIndex()].getName();
            if (!projFieldName.equals(origFieldName)) {
                needRename = true;
            }
            projectedColumns[i] = new Integer(fieldAccess.getIndex());
        }
        numProjectedCols = n;
        return needRename;
    }

    /**
     * Creates a new FennelRenameRel relnode on top of a scan, reflecting
     * renamed columns
     *
     * @return newly created FennelRenameRel
     */
    public RelNode renameProjectedScan(RelNode projectedScan)
    {
        // Replace calling convention with FENNEL_EXEC_CONVENTION
        RelTraitSet traits =
            RelOptUtil.clone(origProject.getTraits());
        traits.setTrait(
            CallingConventionTraitDef.instance,
            FennelRel.FENNEL_EXEC_CONVENTION);

        projectedScan =
            new FennelRenameRel(
                origProject.getCluster(),
                projectedScan,
                fieldNames,
                traits);
        return projectedScan;
    }
}

// End MedAbstractFennelProjectionRule.java