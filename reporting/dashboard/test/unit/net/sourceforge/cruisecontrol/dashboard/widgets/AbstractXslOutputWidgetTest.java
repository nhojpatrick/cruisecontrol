/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2007, ThoughtWorks, Inc.
 * 200 E. Randolph, 25th Floor
 * Chicago, IL 60601 USA
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
package net.sourceforge.cruisecontrol.dashboard.widgets;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.DataUtils;

import org.apache.commons.lang.StringUtils;

/**
 * @author Ketan Padegaonkar
 */
public class AbstractXslOutputWidgetTest extends TestCase {
    public void testShouldBeAbleToGetLogFileAndReturnParsedContent() throws Exception {
        AbstractXslOutputWidget service = new AbstractXslOutputWidget() {

            protected String getXslPath() {
                return "xsl/ant.xsl";
            }

            public String getDisplayName() {
                return null;
            }
        };
        Map params = new HashMap();
        File antLogAsFile = DataUtils.getFailedBuildLbuildAsFile();
        assertTrue(antLogAsFile.exists());
        params.put(Widget.PARAM_BUILD_LOG_FILE, antLogAsFile);
        params.put(Widget.PARAM_CC_ROOT, DataUtils.getCCRoot());
        String output = (String) service.getOutput(params);
        assertTrue(StringUtils.contains(output, "Build Failed"));
        assertTrue(StringUtils.contains(output, "<td class=\"error\" nowrap=\"yes\">Cannot find something</td>"));
        assertTrue(StringUtils.contains(output, "This is my stacktrace"));
    }
}