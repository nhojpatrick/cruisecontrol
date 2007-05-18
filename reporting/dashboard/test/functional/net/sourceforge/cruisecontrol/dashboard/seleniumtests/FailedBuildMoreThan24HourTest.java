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
package net.sourceforge.cruisecontrol.dashboard.seleniumtests;

import java.io.File;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.DataUtils;
import net.sourceforge.cruisecontrol.dashboard.utils.CCDateFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

public class FailedBuildMoreThan24HourTest extends SeleniumTestCase {

    private File longFailed;
    private File failAgain;
    private File successful;
    private File jusfFailed;

    protected void doSetUp() throws Exception {
        File root = DataUtils.getConfigXmlAsFile().getAbsoluteFile().getParentFile();
        File logs = new File(root, "logs");
        File projectWithoutPublishers = new File(logs, "projectWithoutPublishers");
        longFailed = new File(projectWithoutPublishers,
                "log" + CCDateFormatter.yyyyMMddHHmmss(new DateTime().minusDays(2)) + ".xml");
        failAgain = new File(projectWithoutPublishers,
                "log" + CCDateFormatter.yyyyMMddHHmmss(new DateTime().minusMinutes(30)) + ".xml");
        successful = new File(projectWithoutPublishers,
                "log" + CCDateFormatter.yyyyMMddHHmmss(new DateTime().minusMinutes(10)) + "Lbuild.510.xml");
        jusfFailed = new File(projectWithoutPublishers,
                "log" + CCDateFormatter.yyyyMMddHHmmss(new DateTime()) + ".xml");

    }

    protected void doTearDown() throws Exception {
        File[] files = new File[]{longFailed, successful, jusfFailed, failAgain};
        for (int i = 0; i < files.length; i++) {
            try {
                FileUtils.forceDelete(files[i]);
            } catch (Exception e) {
                continue;
            }
        }
    }

    public void testChangeColorWhenFailedDateChanges() throws Exception {
        selenium.open("/dashboard/dashboard?s=1");
        assertClassName(longFailed, "long_failed");
        assertClassName(failAgain, "long_failed");
        assertClassName(successful, "passed");
        assertClassName(jusfFailed, "failed");
    }

    private void assertClassName(File file, String className) throws Exception {
        String exptected = "id=\"tooltip_projectWithoutPublishers\" class=\"tooltip tooltip_" + className + "";
        file.createNewFile();
        Thread.sleep(7000);
        String htmlSource = selenium.getHtmlSource();
        assertTrue(StringUtils.contains(htmlSource, exptected));
    }

}
