/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
package net.sourceforge.cruisecontrol.labelincrementers;

import junit.framework.TestCase;

public class FormattedLabelIncrementerTest extends TestCase {

    private FormattedLabelIncrementer incrementer;
    private FormattedLabelIncrementer noPrefixIncrementer;

    public FormattedLabelIncrementerTest(String name) {
        super(name);
    }

    public void setUp() {
        incrementer = new FormattedLabelIncrementer();
        noPrefixIncrementer = new FormattedLabelIncrementer();
        noPrefixIncrementer.setPrefix(false);
    }

    public void testIsValidLabel() {
        assertTrue(incrementer.isValidLabel("X_88_INT"));
        assertTrue(noPrefixIncrementer.isValidLabel("88_INT"));

    }

    public void testInvalidLabel() {
        assertFalse(incrementer.isValidLabel("x_y"));
        assertFalse(incrementer.isValidLabel("x88"));
        assertFalse(incrementer.isValidLabel("Y_88_FOO"));
        assertFalse(noPrefixIncrementer.isValidLabel("X_88_INT"));
        assertFalse(noPrefixIncrementer.isValidLabel("x88"));
        assertFalse(noPrefixIncrementer.isValidLabel("88_FOO"));
    }
    
    public void testIncrementLabel() {
        assertEquals("X_89_REL", incrementer.incrementLabel("X_88_REL", null));
        assertEquals("89_REL", noPrefixIncrementer.incrementLabel("88_REL", null));
    }

    public void testGetDefaultLabel() {
        assertEquals("CC_1_INT", incrementer.getDefaultLabel());
        assertTrue(incrementer.isValidLabel(incrementer.getDefaultLabel()));
        assertEquals("1_INT", noPrefixIncrementer.getDefaultLabel());
        assertTrue(incrementer.isValidLabel(noPrefixIncrementer.getDefaultLabel()));
    }

    public void testDefaultLabel() {
        incrementer.setDefaultLabel("FOO_69_REL");
        assertEquals("FOO_69_REL", incrementer.getDefaultLabel());
        incrementer.setDefaultLabel("bar_69_REL");
        assertEquals("BAR_69_REL", incrementer.getDefaultLabel());
        noPrefixIncrementer.setDefaultLabel("69_REL");
        assertEquals("69_REL", noPrefixIncrementer.getDefaultLabel());
    }

}