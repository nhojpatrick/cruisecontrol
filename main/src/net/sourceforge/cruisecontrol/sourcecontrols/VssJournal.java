/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
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
package net.sourceforge.cruisecontrol.sourcecontrols;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sourceforge.cruisecontrol.CruiseControlException;
import net.sourceforge.cruisecontrol.Modification;
import net.sourceforge.cruisecontrol.SourceControl;
import net.sourceforge.cruisecontrol.util.ValidationHelper;
import net.sourceforge.cruisecontrol.util.IO;

import org.apache.log4j.Logger;

/**
 * This class handles all VSS-related aspects of determining the modifications since the last good build.
 *
 * This class uses Source Safe Journal files. Unlike the history files that are generated by executing
 * <code>ss.exe history</code>, journal files must be setup by the Source Safe administrator before the point that
 * logging of modifications is to occur.
 *
 * This code has been tested against Visual Source Safe v6.0 build 8383.
 *
 * @author Eli Tucker
 * @author <a href="mailto:alden@thoughtworks.com">alden almagro</a>
 * @author <a href="mailto:jcyip@thoughtworks.com">Jason Yip</a>
 * @author Arun Aggarwal
 * @author Jonny Boman
 * @author <a href="mailto:simon.brandhof@hortis.ch">Simon Brandhof</a>
 */
public class VssJournal implements SourceControl {

    private static final Logger LOG = Logger.getLogger(VssJournal.class);

    private String dateFormat;
    private String timeFormat;
    private SimpleDateFormat vssDateTimeFormat;
    private boolean overridenDateFormat = false;

    private String ssDir = "$/";
    private String journalFile;

    private SourceControlProperties properties = new SourceControlProperties();

    private Date lastBuild;

    private ArrayList modifications = new ArrayList();

    public VssJournal() {
        dateFormat = "MM/dd/yy";
        timeFormat = "hh:mma";
        constructVssDateTimeFormat();
    }
    /**
     * Set the project to get history from
     *
     */
    public void setSsDir(String s) {
        StringBuffer sb = new StringBuffer();
        if (!s.startsWith("$")) {
            sb.append("$");
        }
        if (s.endsWith("/")) {
            sb.append(s.substring(0, s.length() - 1));
        } else {
            sb.append(s);
        }
        this.ssDir = sb.toString();
    }

    /**
     * Full path to journal file. Example: <code>c:/vssdata/journal/journal.txt</code>
     *
     * @param journalFile
     */
    public void setJournalFile(String journalFile) {
        this.journalFile = journalFile;
    }

    /**
     * Choose a property to be set if the project has modifications if we have a change that only requires repackaging,
     * i.e. jsp, we don't need to recompile everything, just rejar.
     *
     * @param property
     */
    public void setProperty(String property) {
        properties.assignPropertyName(property);
    }

    /**
     * Set the name of the property to be set if some files were deleted or renamed from VSS on this project.
     *
     * @param propertyOnDelete the name of the property to set
     */
    public void setPropertyOnDelete(String propertyOnDelete) {
        properties.assignPropertyOnDeleteName(propertyOnDelete);
    }

    /**
     * Sets the date format to use for parsing VSS journal.
     *
     * The default date format is <code>MM/dd/yy</code>. If your VSS server is set to a different region, you may wish
     * to use a format such as <code>dd/MM/yy</code>.
     *
     * @see java.text.SimpleDateFormat
     */
    public void setDateFormat(String format) {
        dateFormat = format;
        overridenDateFormat = true;
        constructVssDateTimeFormat();
    }

    /**
     * Sets the time format to use for parsing VSS journal.
     *
     * The default time format is <code>hh:mma</code> . If your VSS server is set to a different region, you may wish to
     * use a format such as <code>HH:mm</code> .
     *
     * @see java.text.SimpleDateFormat
     */
    public void setTimeFormat(String format) {
        timeFormat = format;
        constructVssDateTimeFormat();
    }

    private void constructVssDateTimeFormat() {
        vssDateTimeFormat = new SimpleDateFormat(dateFormat + " " + timeFormat, Locale.US);
    }


    /**
     * Sets the _lastBuild date. Protected so it can be used by tests.
     */
    protected void setLastBuildDate(Date lastBuild) {
        this.lastBuild = lastBuild;
    }

    public Map getProperties() {
        return properties.getPropertiesAndReset();
    }

    public void validate() throws CruiseControlException {
        ValidationHelper.assertIsSet(journalFile, "journalfile", this.getClass());
        ValidationHelper.assertIsSet(ssDir, "ssdir", this.getClass());
    }

    /**
     * Do the work... I'm writing to a file since VSS will start wrapping lines if I read directly from the stream.
     */
    public List getModifications(Date lastBuild, Date now) {
        this.lastBuild = lastBuild;
        modifications.clear();

        try {
            final BufferedReader br = new BufferedReader(new FileReader(journalFile));
            try {
                String s = br.readLine();
                while (s != null) {
                    ArrayList entry = new ArrayList();
                    entry.add(s);
                    s = br.readLine();
                    while (s != null && !s.equals("")) {
                        entry.add(s);
                        s = br.readLine();
                    }
                    Modification mod = handleEntry(entry);
                    if (mod != null) {
                        modifications.add(mod);
                    }

                    if ("".equals(s)) {
                        s = br.readLine();
                    }
                }
            } finally {
                IO.close(br);
            }
        } catch (Exception e) {
            LOG.warn(e);
        }

        if (modifications.size() > 0) {
            properties.modificationFound();
        }

        LOG.info("Found " + modifications.size() + " modified files");
        return modifications;
    }

    /**
     * Parse individual VSS history entry
     *
     * @param historyEntry
     */
    protected Modification handleEntry(List historyEntry) {
        Modification mod = new Modification("vss");
        String nameAndDateLine = (String) historyEntry.get(2);
        mod.userName = parseUser(nameAndDateLine);
        mod.modifiedTime = parseDate(nameAndDateLine);

        String folderLine = (String) historyEntry.get(0);
        String fileLine = (String) historyEntry.get(3);
        boolean setPropertyOnDelete = false;

        if (!isInSsDir(folderLine)) {
            // We are only interested in modifications to files in the specified ssdir
            return null;
        } else if (isBeforeLastBuild(mod.modifiedTime)) {
            // We are only interested in modifications since the last build
            return null;
        } else if (fileLine.startsWith("Labeled")) {
            // We don't add labels.
            return null;
        } else if (fileLine.startsWith("Checked in")) {
            String fileName = substringFromLastSlash(folderLine);
            String folderName = substringToLastSlash(folderLine);
            Modification.ModifiedFile modfile = mod.createModifiedFile(fileName, folderName);

            modfile.action = "checkin";
            mod.comment = parseComment(historyEntry);
        } else if (fileLine.indexOf(" renamed to ") > -1) {
            // TODO: This is a special case that is really two modifications: deleted and recovered.
            // For now I'll consider it a deleted to force a clean build.
            // I should really make this two modifications.
            setPropertyOnDelete = deleteModification(historyEntry, mod, fileLine, folderLine);
        } else if (fileLine.indexOf(" moved to ") > -1) {
            setPropertyOnDelete = deleteModification(historyEntry, mod, fileLine, folderLine);
        } else {
            String fileName = fileLine.substring(0, fileLine.lastIndexOf(" "));
            Modification.ModifiedFile modfile = mod.createModifiedFile(fileName, folderLine);

            mod.comment = parseComment(historyEntry);

            if (fileLine.endsWith("added")) {
                modfile.action = "add";
            } else if (fileLine.endsWith("deleted")) {
                modfile.action = "delete";
                setPropertyOnDelete = true;
            } else if (fileLine.endsWith("recovered")) {
                modfile.action = "recover";
            } else if (fileLine.endsWith("shared")) {
                modfile.action = "branch";
            }
        }

        if (setPropertyOnDelete) {
            properties.deletionFound();
        }

        return mod;
    }

    private boolean deleteModification(List historyEntry, Modification mod, String fileLine, String folderLine) {
        mod.comment = parseComment(historyEntry);

        String fileName = fileLine.substring(0, fileLine.indexOf(" "));

        Modification.ModifiedFile modfile = mod.createModifiedFile(fileName, folderLine);
        modfile.action = "delete";
        return true;
    }

    /**
     * parse comment from vss history (could be multiline)
     *
     * @param a
     * @return the comment
     */
    private String parseComment(List a) {
        StringBuffer comment = new StringBuffer();
        for (int i = 4; i < a.size(); i++) {
            comment.append(a.get(i)).append(" ");
        }
        return comment.toString().trim();
    }

    /**
     * Parse date/time from VSS file history
     *
     * The nameAndDateLine will look like User: Etucker Date: 6/26/01 Time: 11:53a Sometimes also this User: Aaggarwa
     * Date: 6/29/:1 Time: 3:40p Note the ":" instead of a "0"
     *
     * May give additional DateFormats through the vssjournaldateformat tag. E.g.
     * <code><vssjournaldateformat format="yy-MM-dd hh:mm"/></code>
     *
     * @return Date
     * @param nameAndDateLine
     */
    public Date parseDate(String nameAndDateLine) {
        // Extract date and time into one string with just one space separating the date from the time
        String dateString = nameAndDateLine.substring(
                nameAndDateLine.indexOf("Date:") + 5,
                nameAndDateLine.indexOf("Time:")).trim();

        String timeString = nameAndDateLine.substring(
                nameAndDateLine.indexOf("Time:") + 5).trim();

        if (!overridenDateFormat) {
            // Fixup for weird format
            int indexOfColon = dateString.indexOf("/:");
            if (indexOfColon != -1) {
                dateString = dateString.substring(0, indexOfColon)
                        + dateString.substring(indexOfColon, indexOfColon + 2).replace(':', '0')
                        + dateString.substring(indexOfColon + 2);
            }

        }
        StringBuffer dateToParse = new StringBuffer();
        dateToParse.append(dateString);
        dateToParse.append(" ");
        dateToParse.append(timeString);
        if (!overridenDateFormat) {
            // the am/pm marker of java.text.SimpleDateFormat is 'am' or 'pm'
            // but we have 'a' or 'p' in default VSS logs with default time format
            // (for example '6:08p' instead of '6:08pm')
            dateToParse.append("m");
        }
        try {
            return vssDateTimeFormat.parse(dateToParse.toString());

        } catch (ParseException pe) {
            LOG.error("Could not parse date in VssJournal file : " + dateToParse.toString(), pe);
        }
        return null;
    }

    /**
     * Parse username from VSS file history
     *
     * @param userLine
     * @return the user name who made the modification
     */
    public String parseUser(String userLine) {
        final int startOfUserName = 6;

        try {
            return userLine.substring(startOfUserName, userLine.indexOf("Date: ") - 1).trim();
        } catch (StringIndexOutOfBoundsException e) {
            LOG.error("Unparsable string was: " + userLine);
            throw e;
        }

    }

    /**
     * Returns the substring of the given string from the last "/" character. UNLESS the last slash character is the
     * last character or the string does not contain a slash. In that case, return the whole string.
     */
    public String substringFromLastSlash(String input) {
        int lastSlashPos = input.lastIndexOf("/");
        if (lastSlashPos > 0 && lastSlashPos + 1 <= input.length()) {
            return input.substring(lastSlashPos + 1);
        }

        return input;
    }

    /**
     * Returns the substring of the given string from the beginning to the last "/" character or till the end of the
     * string if no slash character exists.
     */
    public String substringToLastSlash(String input) {
        int lastSlashPos = input.lastIndexOf("/");
        if (lastSlashPos > 0) {
            return input.substring(0, lastSlashPos);
        }

        return input;
    }

    /**
     * Determines if the given folder is in the ssdir specified for this VssJournalElement.
     */
    protected boolean isInSsDir(String path) {
        boolean isInDir = (path.toLowerCase().startsWith(ssDir.toLowerCase()));
        if (isInDir) {
            // exclude similarly prefixed paths
            if (ssDir.equalsIgnoreCase(path) // is exact same as ssDir (this happens)
                    || ('/' == path.charAt(ssDir.length())) // subdirs below matching ssDir
                    || "$/".equalsIgnoreCase(ssDir)) { // everything is included

                // do nothing
            } else {
                // is not really in subdir
                isInDir = false;
            }
        }
        return isInDir;
    }

    /**
     * Determines if the date given is before the last build for this VssJournalElement.
     */
    protected boolean isBeforeLastBuild(Date date) {
        return date.before(lastBuild);
    }
}
