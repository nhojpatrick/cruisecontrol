package net.sourceforge.cruisecontrol;

import java.io.*;
import java.util.*;

public class CruiseControlProperties {

    public static final String PROPS_NAME_JVM_ARG = "cc.props";
    
    public static final String DEFAULT_PROPERTIES_FILENAME = 
        "cruisecontrol.properties";

    private  List auxLogProperties = new ArrayList();
    private  Set buildmaster;
    private  Set notifyOnFailure;
    
    private  File currentBuildStatusFile;

    private  boolean debug;
    private  boolean verbose;    
    private  boolean useEmailMap;    
    private  boolean isIntervalAbsolute;
    private  boolean mapSourceControlUsersToEmail;
    private  boolean spamWhileBroken = true;
    private  long buildInterval;
    private  int cleanBuildEvery;
    
    private  String defaultEmailSuffix;
    private  String mailhost;
    private  String returnAddress;
    private  String emailmapFilename;
    private  String servletURL;
    private  String logDir;
    private  String antFile;
    private  String antTarget;
    private  String cleanAntTarget;
    private  String labelIncrementerClassName;
    private  String reportSuccess;

    public CruiseControlProperties(String propertiesFile) throws IOException {
        
        if (propertiesFile == null 
            || propertiesFile.trim().length() <= 0) {
            propertiesFile = DEFAULT_PROPERTIES_FILENAME;
        }
        
        //Try to load the actual properties file.
        loadProperties(propertiesFile);
    }

    /**
     * Load properties file, see cruisecontrol.properties for descriptions of 
     * properties.
     */
    private  void loadProperties(String propsFileName) throws IOException {
        File propFile = new File(propsFileName);

        if (!propFile.exists()) {
            throw new FileNotFoundException("Properties file \"" + propFile 
                                            + "\" not found");
        }

        Properties props = new Properties();
        props.load(new FileInputStream(propFile));

        //REDTAG - Jason & Paul - Do we have a getCommaDelimitedList elsewhere?
        StringTokenizer st = new StringTokenizer(props.getProperty("auxlogfiles"), ",");
        while (st.hasMoreTokens()) {
            String nextFile = st.nextToken().trim();
            auxLogProperties.add(nextFile);
        }

        try {
            buildInterval = Integer.parseInt(props.getProperty("buildinterval"))*1000;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
            "buildInterval not set correctly in " + propsFileName);
        }

        debug = getBooleanProperty(props, "debug");
        verbose = getBooleanProperty(props, "verbose");
        isIntervalAbsolute = getBooleanProperty(props, "absoluteInterval");
        mapSourceControlUsersToEmail = 
            getBooleanProperty(props, "mapSourceControlUsersToEmail");

        defaultEmailSuffix = props.getProperty("defaultEmailSuffix");
                
        mailhost = props.getProperty("mailhost");
        if(mailhost.equals("")) {        
            throw new IllegalArgumentException(
            "mailhost not set correctly in " + propsFileName);
        }
        
        servletURL = props.getProperty("servletURL");
        if(servletURL.equals("")) {        
            throw new IllegalArgumentException(
            "servletURL not set correctly in " + propsFileName);
        }
        
        returnAddress = props.getProperty("returnAddress");
        if(returnAddress.equals("")) {        
            throw new IllegalArgumentException(
            "returnAddress not set correctly in " + propsFileName);
        }
        
        buildmaster = getSetFromString(props.getProperty("buildmaster"));
        notifyOnFailure = getSetFromString(props.getProperty("notifyOnFailure"));
        
        reportSuccess = props.getProperty("reportSuccess","always");
        if (!reportSuccess.equalsIgnoreCase("always") &&
            !reportSuccess.equalsIgnoreCase("fixes") &&
            !reportSuccess.equalsIgnoreCase("never")) {
                throw new IllegalArgumentException(
                "invalid value for reportSuccess in " + propsFileName);
        }
        
        spamWhileBroken = getBooleanProperty(props, "spamWhileBroken");

        logDir = props.getProperty("logDir"); 
        new File(logDir).mkdirs();

        String buildStatusFileName = logDir + File.separator 
                                     + props.getProperty("currentBuildStatusFile");
        currentBuildStatusFile = new File(buildStatusFileName);
        currentBuildStatusFile.createNewFile();

        antFile = props.getProperty("antfile");
        antTarget = props.getProperty("target");
        cleanAntTarget = props.getProperty("cleantarget");
        
        try {
            cleanBuildEvery = Integer.parseInt(props.getProperty("cleanBuildEvery"));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
            "cleanBuildEvery not set correctly in " + propsFileName);
        }
        
        labelIncrementerClassName = props.getProperty("labelIncrementerClass");
        if (labelIncrementerClassName == null) {
            labelIncrementerClassName = DefaultLabelIncrementer.class.getName();
        }

        emailmapFilename = props.getProperty("emailmap");
        useEmailMap = usingEmailMap(emailmapFilename);

        if (debug || verbose)
            props.list(System.out);
    }

    private  boolean getBooleanProperty(Properties props, String key) {
        try {
            return props.getProperty(key).equals("true");
        } catch (NullPointerException npe) {
            return false;
        }
    }

    /**
     * Forms a set of unique words/names from the comma
     * delimited list provided. Maybe empty, never null.
     * 
     * @param commaDelim String containing a comma delimited list of words,
     *                   e.g. "paul,Paul, Tim, Alden,,Frank".
     * @return Set of words; maybe empty, never null.
     */
    private  Set getSetFromString(String commaDelim) {
        Set elements = new TreeSet();
        if (commaDelim == null) {
            return elements;
        }

        StringTokenizer st = new StringTokenizer(commaDelim, ",");
        while (st.hasMoreTokens()) {
            String mapped = st.nextToken().trim();
            elements.add(mapped);
        }

        return elements;
    }

    /**
     * This method infers from the value of the email
     * map filename, whether or not the email map is being
     * used. For example, if the filename is blank
     * or null, then the map is not being used.
     * 
     * @param emailMapFileName
     *               Name provided by the user.
     * @return true if the email map should be consulted, otherwise false.
     */
    private  boolean usingEmailMap(String emailMapFileName) {
        //If the user specified name is null or blank or doesn't exist, then 
        //  the email map is not being used.
        if (emailMapFileName == null || emailMapFileName.trim().length() == 0) {
            return false;
        }
        //Otherwise, check to see if the filename provided exists and is readable.
        File userEmailMap = new File(emailMapFileName);
        return userEmailMap.exists() && userEmailMap.canRead();
    }

    /** Getter for property antFile.
     * @return Value of property antFile.
     */
    public  String getAntFile() {
        return antFile;
    }

    /** Setter for property antFile.
     * @param antFile New value of property antFile.
     */
    public  void setAntFile(String antFile) {
        this.antFile = antFile;
    }

    /** Getter for property antTarget.
     * @return Value of property antTarget.
     */
    public  String getAntTarget() {
        return antTarget;
    }

    /** Setter for property antTarget.
     * @param antTarget New value of property antTarget.
     */
    public  void setAntTarget(String antTarget) {
        this.antTarget = antTarget;
    }

    /** Getter for property auxLogProperties.
     * @return Value of property auxLogProperties.
     */
    public  List getAuxLogProperties() {
        return auxLogProperties;
    }

    /** Setter for property auxLogProperties.
     * @param auxLogProperties New value of property auxLogProperties.
     */
    public  void setAuxLogProperties(List auxLogProperties) {
        this.auxLogProperties = auxLogProperties;
    }

    /** Getter for property buildInterval.
     * @return Value of property buildInterval.
     */
    public  long getBuildInterval() {
        return buildInterval;
    }

    /** Setter for property buildInterval.
     * @param buildInterval New value of property buildInterval.
     */
    public  void setBuildInterval(long buildInterval) {
        this.buildInterval = buildInterval;
    }

    /** Getter for property buildmaster.
     * @return Value of property buildmaster.
     */
    public  Set getBuildmaster() {
        return buildmaster;
    }

    /** Setter for property buildmaster.
     * @param buildmaster New value of property buildmaster.
     */
    public  void setBuildmaster(Set buildmaster) {
        this.buildmaster = buildmaster;
    }

    /** Getter for property cleanAntTarget.
     * @return Value of property cleanAntTarget.
     */
    public  String getCleanAntTarget() {
        return cleanAntTarget;
    }

    /** Setter for property cleanAntTarget.
     * @param cleanAntTarget New value of property cleanAntTarget.
     */
    public  void setCleanAntTarget(String cleanAntTarget) {
        this.cleanAntTarget = cleanAntTarget;
    }

    /** Getter for property cleanBuildEvery.
     * @return Value of property cleanBuildEvery.
     */
    public  int getCleanBuildEvery() {
        return cleanBuildEvery;
    }

    /** Setter for property cleanBuildEvery.
     * @param cleanBuildEvery New value of property cleanBuildEvery.
     */
    public  void setCleanBuildEvery(int cleanBuildEvery) {
        this.cleanBuildEvery = cleanBuildEvery;
    }

    /** Getter for property currentBuildStatusFile.
     * @return Value of property currentBuildStatusFile.
     */
    public  java.io.File getCurrentBuildStatusFile() {
        return currentBuildStatusFile;
    }

    /** Setter for property currentBuildStatusFile.
     * @param currentBuildStatusFile New value of property currentBuildStatusFile.
     */
    public  void setCurrentBuildStatusFile(java.io.File currentBuildStatusFile) {
        this.currentBuildStatusFile = currentBuildStatusFile;
    }

    /** Getter for property debug.
     * @return Value of property debug.
     */
    public  boolean isDebug() {
        return debug;
    }

    /** Setter for property debug.
     * @param debug New value of property debug.
     */
    public  void setDebug(boolean debug) {
        this.debug = debug;
    }

    /** Getter for property defaultEmailSuffix.
     * @return Value of property defaultEmailSuffix.
     */
    public  String getDefaultEmailSuffix() {
        return defaultEmailSuffix;
    }

    /** Setter for property defaultEmailSuffix.
     * @param defaultEmailSuffix New value of property defaultEmailSuffix.
     */
    public  void setDefaultEmailSuffix(String defaultEmailSuffix) {
        this.defaultEmailSuffix = defaultEmailSuffix;
    }

    /** Getter for property emailmapFilename.
     * @return Value of property emailmapFilename.
     */
    public  String getEmailmapFilename() {
        return emailmapFilename;
    }

    /** Setter for property emailmapFilename.
     * @param emailmapFilename New value of property emailmapFilename.
     */
    public  void setEmailmapFilename(String emailmapFilename) {
        this.emailmapFilename = emailmapFilename;
    }

    /** Getter for property isIntervalAbsolute.
     * @return Value of property isIntervalAbsolute.
     */
    public  boolean isIntervalAbsolute() {
        return isIntervalAbsolute;
    }

    /** Setter for property isIntervalAbsolute.
     * @param isIntervalAbsolute New value of property isIntervalAbsolute.
     */
    public  void setIsIntervalAbsolute(boolean isIntervalAbsolute) {
        this.isIntervalAbsolute = isIntervalAbsolute;
    }

    /** Getter for property labelIncrementerClassName.
     * @return Value of property labelIncrementerClassName.
     */
    public  String getLabelIncrementerClassName() {
        return labelIncrementerClassName;
    }

    /** Setter for property labelIncrementerClassName.
     * @param labelIncrementerClassName New value of property labelIncrementerClassName.
     */
    public  void setLabelIncrementerClassName(String labelIncrementerClassName) {
        this.labelIncrementerClassName = labelIncrementerClassName;
    }

    /** Getter for property logDir.
     * @return Value of property logDir.
     */
    public  String getLogDir() {
        return logDir;
    }

    /** Setter for property logDir.
     * @param logDir New value of property logDir.
     */
    public  void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    /** Getter for property mailhost.
     * @return Value of property mailhost.
     */
    public  String getMailhost() {
        return mailhost;
    }

    /** Setter for property mailhost.
     * @param mailhost New value of property mailhost.
     */
    public  void setMailhost(String mailhost) {
        this.mailhost = mailhost;
    }

    /** Getter for property mapSourceControlUsersToEmail.
     * @return Value of property mapSourceControlUsersToEmail.
     */
    public  boolean isMapSourceControlUsersToEmail() {
        return mapSourceControlUsersToEmail;
    }

    /** Setter for property mapSourceControlUsersToEmail.
     * @param mapSourceControlUsersToEmail New value of property mapSourceControlUsersToEmail.
     */
    public  void setMapSourceControlUsersToEmail(boolean mapSourceControlUsersToEmail) {
        this.mapSourceControlUsersToEmail = mapSourceControlUsersToEmail;
    }

    /** Getter for property notifyOnFailure.
     * @return Value of property notifyOnFailure.
     */
    public  Set getNotifyOnFailure() {
        return notifyOnFailure;
    }

    /** Setter for property notifyOnFailure.
     * @param notifyOnFailure New value of property notifyOnFailure.
     */
    public  void setNotifyOnFailure(Set notifyOnFailure) {
        this.notifyOnFailure = notifyOnFailure;
    }

    /** Getter for property reportSuccess.
     * @return Value of property reportSuccess.
     */
    public  String getReportSuccess() {
        return reportSuccess;
    }

    /** Setter for property reportSuccess.
     * @param reportSuccess New value of property reportSuccess.
     */
    public  void setReportSuccess(String reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    /** Getter for property returnAddress.
     * @return Value of property returnAddress.
     */
    public  String getReturnAddress() {
        return returnAddress;
    }

    /** Setter for property returnAddress.
     * @param returnAddress New value of property returnAddress.
     */
    public  void setReturnAddress(String returnAddress) {
        this.returnAddress = returnAddress;
    }

    /** Getter for property servletURL.
     * @return Value of property servletURL.
     */
    public  String getServletURL() {
        return servletURL;
    }

    /** Setter for property servletURL.
     * @param servletURL New value of property servletURL.
     */
    public  void setServletURL(String servletURL) {
        this.servletURL = servletURL;
    }

    /** Getter for property spamWhileBroken.
     * @return Value of property spamWhileBroken.
     */
    public  boolean shouldSpamWhileBroken() {
        return spamWhileBroken;
    }

    /** Setter for property spamWhileBroken.
     * @param spamWhileBroken New value of property spamWhileBroken.
     */
    public  void setSpamWhileBroken(boolean spamWhileBroken) {
        this.spamWhileBroken = spamWhileBroken;
    }

    /** Getter for property useEmailMap.
     * @return Value of property useEmailMap.
     */
    public  boolean useEmailMap() {
        return useEmailMap;
    }

    /** Setter for property useEmailMap.
     * @param useEmailMap New value of property useEmailMap.
     */
    public  void setUseEmailMap(boolean useEmailMap) {
        this.useEmailMap = useEmailMap;
    }

    /** Getter for property verbose.
     * @return Value of property verbose.
     */
    public  boolean isVerbose() {
        return verbose;
    }

    /** Setter for property verbose.
     * @param verbose New value of property verbose.
     */
    public  void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
