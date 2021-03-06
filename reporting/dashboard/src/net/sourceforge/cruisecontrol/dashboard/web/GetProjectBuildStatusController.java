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
package net.sourceforge.cruisecontrol.dashboard.web;

import net.sourceforge.cruisecontrol.dashboard.service.BuildSummaryUIService;
import net.sourceforge.cruisecontrol.dashboard.service.LatestBuildSummariesService;
import net.sourceforge.cruisecontrol.dashboard.web.command.BuildCommand;
import net.sourceforge.cruisecontrol.dashboard.web.view.JsonView;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GetProjectBuildStatusController implements Controller {
    public static final String PROJECT_STATUS_IN_BUILDING = "Building";

    private static final Logger LOGGER = Logger.getLogger(GetProjectBuildStatusController.class);

    public static final String CACHE_CONTROL = "max-age=1, no-cache";

    private final LatestBuildSummariesService buildSummariesSerivce;

    private final BuildSummaryUIService uiService;

    public GetProjectBuildStatusController(LatestBuildSummariesService buildSummarySerivce,
                                           BuildSummaryUIService uiService) {
        this.buildSummariesSerivce = buildSummarySerivce;
        this.uiService = uiService;
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
        Map buildInfo = new HashMap();
        try {
            List projectsBuildSummaries = buildSummariesSerivce.getLatestOfProjects();
            List buildSummaryCommands = uiService.transformWithLevel(projectsBuildSummaries);
            buildInfo.put(JsonView.RENDER_DIRECT, createBuildInfos(buildSummaryCommands));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn(e);
            buildInfo.put("error", e.getMessage());
        }
        // This will force the browser to clear the cache only for this page.
        // If any other pages need to clear the cache, we might want to move this
        // logic to an intercepter.
        response.addHeader("Cache-Control", CACHE_CONTROL);
        return new ModelAndView(new JsonView(), new HashMap(buildInfo));
    }

    private List createBuildInfos(List commands) {
        List infos = new ArrayList();
        for (Iterator iter = commands.iterator(); iter.hasNext();) {
            BuildCommand command = (BuildCommand) iter.next();
            Map info = new HashMap();
            info.put("building_info", command.toJsonHash());
            infos.add(info);
        }
        return infos;
    }
}
