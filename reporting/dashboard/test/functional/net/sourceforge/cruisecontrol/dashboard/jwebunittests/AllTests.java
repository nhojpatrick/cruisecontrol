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
package net.sourceforge.cruisecontrol.dashboard.jwebunittests;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junitx.util.DirectorySuiteBuilder;
import junitx.util.SimpleTestFilter;
import net.sourceforge.cruisecontrol.dashboard.testhelpers.CruiseDashboardServer;

public final class AllTests {

    private static final CruiseDashboardServer SERVER = new CruiseDashboardServer();

    private AllTests() {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.thoughtworks.cruisepanel") {
            public void run(TestResult arg0) {
                try {
                    SERVER.start();
                    super.run(arg0);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        SERVER.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        DirectorySuiteBuilder builder = new DirectorySuiteBuilder();
        builder.setFilter(new SimpleTestFilter() {
            public boolean include(String arg0) {
                return (arg0.indexOf(File.separator + "seleniumtests" + File.separator) < 0);
            }
        });
        Test allTests = null;
        try {
            allTests = builder.suite("target/classes/functionaltest/");
            suite.addTest(allTests);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suite;
    }
}
