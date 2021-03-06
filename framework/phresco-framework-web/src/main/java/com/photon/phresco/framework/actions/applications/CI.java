/*
 * ###
 * Framework Web Archive
 * 
 * Copyright (C) 1999 - 2012 Photon Infotech Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ###
 */
package com.photon.phresco.framework.actions.applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.CronExpression;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.photon.phresco.commons.CIJob;
import com.photon.phresco.commons.CIJobStatus;
import com.photon.phresco.commons.CIPasswordScrambler;
import com.photon.phresco.commons.FrameworkConstants;
import com.photon.phresco.configuration.Environment;
import com.photon.phresco.framework.PhrescoFrameworkFactory;
import com.photon.phresco.framework.actions.FrameworkBaseAction;
import com.photon.phresco.framework.api.ActionType;
import com.photon.phresco.framework.api.Project;
import com.photon.phresco.framework.api.ProjectAdministrator;
import com.photon.phresco.framework.api.ProjectRuntimeManager;
import com.photon.phresco.framework.commons.ApplicationsUtil;
import com.photon.phresco.framework.commons.DiagnoseUtil;
import com.photon.phresco.framework.commons.FrameworkUtil;
import com.photon.phresco.framework.commons.LogErrorReport;
import com.photon.phresco.framework.commons.PBXNativeTarget;
import com.photon.phresco.framework.commons.filter.FileListFilter;
import com.photon.phresco.model.CIBuild;
import com.photon.phresco.util.AndroidConstants;
import com.photon.phresco.util.TechnologyTypes;
import com.photon.phresco.util.Utility;
import com.photon.phresco.util.XCodeConstants;


public class CI extends FrameworkBaseAction implements FrameworkConstants {

	private static final long serialVersionUID = -2040671011555139339L;
    private static final Logger S_LOGGER= Logger.getLogger(CI.class);
    private static Boolean debugEnabled  = S_LOGGER.isDebugEnabled();

    private String name = null;
    private String svnurl = null;
    private String username = null;
    private String password = null;
    private String[] emails = null;
    private String successEmailIds = null;
    private String failureEmailIds = null;
    private String schedule = null;
    private String cronExpression = null;
    private String projectCode = null;
	private String showSettings = null;
    private List<String> serverSettings = null;
	private List<String> dbSettings = null;
    private List<String> websrvcSettings = null;
    private List<String> emailSettings = null;
    private String database = null;
	private String server = null;
	private String email = null;
	private String webservice = null;
	private String buildDownloadUrl = null;
	private InputStream fileInputStream;
	private String fileName = "";
	private String senderEmailId = null;
	private String senderEmailPassword = null;
	private int totalBuildSize;
	boolean buildInProgress = false;
	
	private String environment = null;
	private String sdk = null;
	private String mode = null;
	private String androidVersion = null;
	
	private String target = "";
	private String proguard = null;
	private List<String> triggers = null;
    CIJob job = null;

    public String ci() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method CI.ci()");
		}
    	try {
    		boolean jenkinsAlive = false;
    		boolean buildJenkinsAlive = false;
    		List<CIBuild> builds = null;
            ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = (Project) administrator.getProject(projectCode);
            getHttpRequest().setAttribute("project", project);
            getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
            getHttpRequest().setAttribute("jenkinsAlive", jenkinsAlive);
            getHttpRequest().setAttribute("buildJenkinsAlive", buildJenkinsAlive);
            
            jenkinsAlive = DiagnoseUtil.isConnectionAlive("http", "localhost", Integer.parseInt(getPortNo(Utility.getJenkinsHome())));
            getHttpRequest().setAttribute("jenkinsAlive", jenkinsAlive);
            
            CIJob existJob = administrator.getJob(project);
			getHttpRequest().setAttribute(REQ_EXISTING_JOB, existJob);
			if(existJob != null) {
				buildJenkinsAlive = DiagnoseUtil.isConnectionAlive("http", existJob.getJenkinsUrl(), Integer.parseInt(existJob.getJenkinsPort()));
		    	if (debugEnabled) {
					S_LOGGER.debug("Build jenkins alive " + buildJenkinsAlive);
				}
				
				getHttpRequest().setAttribute("buildJenkinsAlive", buildJenkinsAlive);
				if(buildJenkinsAlive == true) {
					builds = administrator.getBuilds(project);
		            if(administrator.getProgressInBuild(project) > 0) {
		            	buildInProgress = true;
		            }
				}
			}
			getHttpRequest().setAttribute(CI_JOB_JSON_BUILDS, builds);
			getHttpRequest().setAttribute(REQ_CI_BUILD_PROGRESS, buildInProgress);
			
        	if (debugEnabled) {
        		S_LOGGER.debug("jenkinsAlive " + jenkinsAlive);
    		}
//            getHttpRequest().setAttribute("jenkinsAlive", jenkinsAlive);
			
		} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.ci()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        }
        return APP_CI;
    }
    
    public String configure() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.configure()");
		}
    	try {
    	    ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
            // Get environment info
			List<Environment> environments = administrator.getEnvironments(project);
			getHttpRequest().setAttribute(REQ_ENVIRONMENTS, environments);
            getHttpRequest().setAttribute(REQ_PROJECT, project);
			// Get xcode targets
            String technology = project.getProjectInfo().getTechnology().getId();
			if (TechnologyTypes.IPHONES.contains(technology)) {
				List<PBXNativeTarget> xcodeConfigs = ApplicationsUtil.getXcodeConfiguration(projectCode);
				getHttpRequest().setAttribute(REQ_XCODE_CONFIGS, xcodeConfigs);
			}
			
			CIJob existJob = administrator.getJob(project);
			getHttpRequest().setAttribute(REQ_EXISTING_JOB, existJob);
	    	getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
	    	getHttpRequest().setAttribute(REQ_PROJECT_CODE , projectCode);
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.configure()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        	new LogErrorReport(e, "CI configuration clicked");
    	}
        return APP_CI_CONFIGURE;
    }
    
    public String setup() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.setup()");
		}
    	try {
    		ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
    		Project project = administrator.getProject(projectCode);
    		ActionType actionType = ActionType.JENKINS_SETUP;
    		actionType.setWorkingDirectory(Utility.getJenkinsHome());
        	if (debugEnabled) {
        		S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
    		}
        	
        	// Here we have to place two files in jenkins_home environment variable location
        	administrator.getJdkHomeXml();
        	administrator.getMavenHomeXml(); 
        	// place email ext plugin in plugin folder
        	administrator.getEmailExtPlugin();
            BufferedReader reader = runtimeManager.performAction(project, actionType, null, null);
            getHttpSession().setAttribute(projectCode + CI_SETUP, reader);
            getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
            getHttpRequest().setAttribute(REQ_TEST_TYPE, CI_SETUP );
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.setup()" + FrameworkUtil.getStackTraceAsString(e));
    		}
    	}
        return APP_ENVIRONMENT_READER;
    }
    
    public String save() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.save()");
		}
    	return doUpdateSave(CI_CREATE_JOB_COMMAND);
    }

    public String update() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.update()");
		}
        return doUpdateSave(CI_UPDATE_JOB_COMMAND);
    }
    
    public String doUpdateSave(String jobType) {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.doUpdateSave()");
		}
    	try {
    	    ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
            String technology = project.getProjectInfo().getTechnology().getId();
            CIJob existJob = administrator.getJob(project);
            if(existJob == null) {
            	existJob = new CIJob();
            }
//    		job = new CIJob(name, svnurl, username, CIPasswordScrambler.mask(password));
            InetAddress thisIp =InetAddress.getLocalHost();
    		existJob.setName(name);
    		existJob.setSvnUrl(svnurl);
    		existJob.setUserName(username);
    		existJob.setPassword(CIPasswordScrambler.mask(password));
    		existJob.setJenkinsUrl(thisIp.getHostAddress());
    		existJob.setJenkinsPort(getPortNo(Utility.getJenkinsHome()));
    		existJob.setTriggers(triggers);
    		Map<String, String> emails = new HashMap<String, String>(2);
    		emails.put(REQ_KEY_SUCCESS_EMAILS, successEmailIds);
    		emails.put(REQ_KEY_FAILURE_EMAILS, failureEmailIds);
    		
			Map<String, String> settingsInfoMap = new HashMap<String, String>(2);
			ActionType actionType = null;
			
			//For web technologies
			if(StringUtils.isNotEmpty(environment)) {
				settingsInfoMap.put(ENVIRONMENT_NAME, environment);
			}
			
			// For android technologies
			if (StringUtils.isNotEmpty(androidVersion)) {
				settingsInfoMap.put(AndroidConstants.ANDROID_VERSION_MVN_PARAM,	androidVersion);
			}
			
			// For iphone technoloies
			if (TechnologyTypes.IPHONES.contains(technology)) {
				settingsInfoMap.put(IPHONE_SDK, sdk);
				settingsInfoMap.put(IPHONE_CONFIGURATION, mode);
				settingsInfoMap.put(IPHONE_TARGET_NAME, target);
				if (TechnologyTypes.IPHONE_HYBRID.equals(technology)) {
					settingsInfoMap.put(IPHONE_PLISTFILE, XCodeConstants.HYBRID_PLIST);
				} else if (TechnologyTypes.IPHONE_NATIVE.equals(technology)) {
					settingsInfoMap.put(IPHONE_PLISTFILE, XCodeConstants.NATIVE_PLIST);
				}
			}

			// For mobile technologies
			if (TechnologyTypes.ANDROIDS.contains(technology)) {
				if (StringUtils.isEmpty(proguard)) {
					// if the checkbox is selected value should be set to false otherwise true
					proguard = TRUE;
				}
				/*settingsInfoMap.put(ANDROID_PROGUARD_SKIP, proguard);*/
				actionType = ActionType.MOBILE_COMMON_COMMAND;
			} else if (TechnologyTypes.IPHONES.contains(technology)) {
				actionType = ActionType.IPHONE_BUILD_UNIT_TEST;
			} else {
				actionType = ActionType.BUILD;
			}
    		ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
    		String mvncmd = "";
    		StringBuilder command = actionType.getCommand();
    		StringBuilder buildMavenCommand = new StringBuilder();
    		if (command == null) { 
    			 buildMavenCommand = runtimeManager.buildMavenCommand(project, actionType, settingsInfoMap);
    		} else {
    			buildMavenCommand.append(command);
    			buildMavenCommand.append(runtimeManager.buildMavenArgCommand(actionType, settingsInfoMap));
    		}
    		mvncmd = buildMavenCommand.toString().substring(4).trim();
    		existJob.setMvnCommand(mvncmd);
    		existJob.setEmail(emails);
    		existJob.setScheduleType(schedule);
    		existJob.setScheduleExpression(cronExpression);
    		existJob.setSenderEmailId(senderEmailId);
    		existJob.setSenderEmailPassword(senderEmailPassword);
    		
    		if(CI_CREATE_JOB_COMMAND.equals(jobType)) {
    			administrator.createJob(project, existJob);
        		addActionMessage(getText(SUCCESS_JOB));
    		} else if(CI_UPDATE_JOB_COMMAND.equals(jobType)) {
        		administrator.updateJob(project, existJob);
        		addActionMessage(getText(SUCCESS_UPDATE));
    		}
    		
    		restartJenkins(); // TODO: reload config
    		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.doUpdateSave()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        	addActionMessage(getText(CI_SAVE_UPDATE_FAILED, e.getLocalizedMessage()));
//        	new LogErrorReport(e, "CI creation / updation failed");
    	}
    	
    	return ci();
    	
    }
    
    public String build() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.build()");
		}
    	try {
    	    ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
            getHttpRequest().setAttribute("totalBuildsSize", administrator.getTotalBuilds(project)+"");
            // clear existin do_not_checkin_folder
            administrator.deleteDoNotCheckin(project);
            
    		CIJobStatus ciJobStatus = administrator.buildJob(project);
    		if(ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
            	if (debugEnabled) {
            		S_LOGGER.debug("Jenkins build job status code " + ciJobStatus.getCode());
        		}
    			addActionError(getText(ciJobStatus.getMessage()));
    		} else {
            	if (debugEnabled) {
            		S_LOGGER.debug("Jenkins build job status code " + ciJobStatus.getCode());
        		}
    			addActionMessage(getText(ciJobStatus.getMessage()));
    		}
    		getHttpRequest().setAttribute("buildStatus", "true");
    		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
    	} catch (Exception e) {
    		getHttpRequest().setAttribute("buildStatus", "false");
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.build()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        	addActionMessage(getText(CI_BUILD_FAILED, e.getLocalizedMessage()));        	
//        	new LogErrorReport(e, "CI build failed");
    	}
    	return ci();
    }
    
    public String getTotalBuilds() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.getTotalBuilds()");
		}
    	try {
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
    		totalBuildSize = administrator.getTotalBuilds(project); // when getting all the builds , it ll try to get all build status, so it ll return -1.
    	} catch (Exception e) {
    		totalBuildSize = -1;
    	}
    	return "success";
    }
    
    
    public String deleteCIBuild(){
    	if (debugEnabled) {
			S_LOGGER.debug("Entering Method  CI.deleteCIBuild()");
		}
        String[] selectedBuilds = getHttpRequest().getParameterValues(REQ_SELECTED_BUILDS);
        List<String> builds = new ArrayList<String>();
        builds = Arrays.asList(selectedBuilds);
        try {
            ProjectAdministrator administrator = getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
            CIJobStatus ciJobStatus = administrator.deleteCI(project, builds);
    		if(ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
            	if (debugEnabled) {
        			S_LOGGER.debug("Jenkins delete build status code " + ciJobStatus.getCode());
        		}
    			addActionError(getText(ciJobStatus.getMessage()));
    		} else {
            	if (debugEnabled) {
        			S_LOGGER.debug("Jenkins delete build status code " + ciJobStatus.getCode());
        		}
    			addActionMessage(getText(ciJobStatus.getMessage()));
    		}
            getHttpRequest().setAttribute("project", project);
        } catch (Exception e) {
        	if (debugEnabled) {
                S_LOGGER.error("Entered into catch block of CI.deleteCIBuild()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        	new LogErrorReport(e, "Build delete");
        }
        getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
        return ci();
    }
    
    public String deleteCIJob(){
    	if (debugEnabled) {
			S_LOGGER.debug("Entering Method  CI.deleteCIJob()");
		}
        try {
            ProjectAdministrator administrator = getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
            CIJobStatus ciJobStatus = administrator.deleteCI(project, null);
    		if(ciJobStatus.getCode() == JOB_STATUS_NOTOK) {
            	if (debugEnabled) {
        			S_LOGGER.debug("Jenkins delete job status code " + ciJobStatus.getCode());
        		}
    			addActionError(getText(ciJobStatus.getMessage()));
    		} else {
            	if (debugEnabled) {
        			S_LOGGER.debug("Jenkins delete job status code " + ciJobStatus.getCode());
        		}
    			addActionMessage(getText(ciJobStatus.getMessage()));
    		}
            getHttpRequest().setAttribute("project", project);
        } catch (Exception e) {
        	if (debugEnabled) {
                S_LOGGER.error("Entered into catch block of CI.deleteCIJob()" + FrameworkUtil.getStackTraceAsString(e));
    		}
        	new LogErrorReport(e, "Job delete");
        }
        getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
        return ci();
    }
    
    public String startJenkins() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.startJenkins()");
		}
    	try {
    		ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
    		ActionType actionType = ActionType.JENKINS_START;
       		actionType.setWorkingDirectory(Utility.getJenkinsHome());
        	if (debugEnabled) {
        		S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
    		}
    		BufferedReader reader = runtimeManager.performAction(project, actionType, null, null);
    		
            getHttpSession().setAttribute(projectCode + CI_START, reader);
            getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
            getHttpRequest().setAttribute(REQ_TEST_TYPE, CI_START );
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.startJenkins()" + FrameworkUtil.getStackTraceAsString(e));
    		}
    	}
    	return APP_ENVIRONMENT_READER;
    }

    public String stopJenkins() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.stopJenkins()");
		}
    	try {
    		ProjectRuntimeManager runtimeManager = PhrescoFrameworkFactory.getProjectRuntimeManager();
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
            Project project = administrator.getProject(projectCode);
    		ActionType actionType = ActionType.JENKINS_STOP;
       		actionType.setWorkingDirectory(Utility.getJenkinsHome());
        	if (debugEnabled) {
        		S_LOGGER.debug("Jenkins Home " + Utility.getJenkinsHome().toString());
    		}
    		BufferedReader reader = runtimeManager.performAction(project, actionType, null, null);

            getHttpSession().setAttribute(projectCode + CI_STOP, reader);
            getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
            getHttpRequest().setAttribute(REQ_TEST_TYPE, CI_STOP );
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.stopJenkins()" + FrameworkUtil.getStackTraceAsString(e));
    		}
    	}
    	return APP_ENVIRONMENT_READER;
    }
    
    public String restartJenkins() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.restartJenkins()");
		}
		try {
    	stopJenkins();
    	if (debugEnabled) {
    		S_LOGGER.debug("stopJenkins method called");
		}
    	BufferedReader reader = (BufferedReader) getHttpSession().getAttribute(projectCode + CI_STOP);
		String line;
		line = reader.readLine();
		while (!line.startsWith("[INFO] BUILD SUCCESS")) {
			line = reader.readLine();
	    	if (debugEnabled) {
	    		S_LOGGER.debug("Restart Stop Console : " + line);
			}
		}
    	waitForTime(5);
    	
    	startJenkins();
    	if (debugEnabled) {
    		S_LOGGER.debug("startJenkins method called");
		}
    	
    	BufferedReader reader1 = (BufferedReader) getHttpSession().getAttribute(projectCode + CI_START);
		String line1;
		line1 = reader1.readLine();
		while (!line1.startsWith("[INFO] BUILD SUCCESS")) {
			line1 = reader1.readLine();
	    	if (debugEnabled) {
	    		S_LOGGER.debug("Restart Start Console : " + line);
			}
		}
		waitForTime(2);
		
		} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.restartJenkins()" + FrameworkUtil.getStackTraceAsString(e));
    		}
		}
		
    	return "restarted";
    }
    
    public void waitForTime(int waitSec) {
    	if (debugEnabled) {
    		S_LOGGER.debug("waitForTime : " + waitSec);
		}
		long startTime = 0;
		startTime = new Date().getTime();
		while (new Date().getTime() < startTime + waitSec * 1000) {
			//Dont do anything for some seconds. It waits till the log is written to file
		}
    	if (debugEnabled) {
    		S_LOGGER.debug("Return from waitForTime ");
		}
	}
    
    public String cronValidation() {
    	if (debugEnabled) {
    		S_LOGGER.debug("Entering Method  CI.cronValidation()");
		}
    	try {
    		HttpServletRequest request = getHttpRequest();
    		String cronBy = request.getParameter(REQ_CRON_BY);
    	    String jobName = "Job Name";
    	    String cronExpression = "";
    	    Date[] dates = null;
    	    
            if (REQ_CRON_BY_DAILY.equals(cronBy)) {
                String hours = request.getParameter(REQ_HOURS);
                String minutes = request.getParameter(REQ_MINUTES);
                String every = request.getParameter(REQ_SCHEDULE_EVERY);

                if ("false".equals(every)) {
                    if ("*".equals(hours) && "*".equals(minutes)) {
                        cronExpression = "0 * * * * ?";
                    } else if ("*".equals(hours) && !"*".equals(minutes)) {
                        cronExpression = "0 " + minutes + " 0 * * ?";
                    } else if (!"*".equals(hours) && "*".equals(minutes)) {
                        cronExpression = "0 0 " + hours + " * * ?";
                    } else if (!"*".equals(hours) && !"*".equals(minutes)) {
                        cronExpression = "0 " + minutes + " " + hours + " * * ?";
                    }
                } else {
                    if ("*".equals(hours) && "*".equals(minutes)) {
                        cronExpression = "0 * * * * ?";
                    } else if ("*".equals(hours) && !"*".equals(minutes)) {
                        cronExpression = "0 " + "*/" + minutes + " * * * ?"; // 0 replace with *
                    } else if (!"*".equals(hours) && "*".equals(minutes)) {
                        cronExpression = "0 0 " + "*/" + hours + " * * ?";	// 0 replace with *
                    } else if (!"*".equals(hours) && !"*".equals(minutes)) {
                        cronExpression = "0 " + minutes + " */" + hours + " * * ?"; // 0 replace with *
                    }
                }
                dates = testCronExpression(cronExpression); 

            } else if (REQ_CRON_BY_WEEKLY.equals(cronBy)) {
                String hours = request.getParameter(REQ_HOURS);
                String minutes = request.getParameter(REQ_MINUTES);
                String week = request.getParameter(REQ_CRON_BY_WEEK);
                hours = ("*".equals(hours)) ? "0" : hours;
                minutes = ("*".equals(minutes)) ? "0" : minutes;
                cronExpression = "0 " + minutes + " " + hours + " ? * " + week;
                dates = testCronExpression(cronExpression);

            } else if (REQ_CRON_BY_MONTHLY.equals(cronBy)) {
                String hours = request.getParameter(REQ_HOURS);
                String minutes = request.getParameter(REQ_MINUTES);
                String month = request.getParameter(REQ_MONTH);
                String day = request.getParameter(REQ_DAY);
                hours = ("*".equals(hours)) ? "0" : hours;
                minutes = ("*".equals(minutes)) ? "0" : minutes;
                cronExpression = "0 " + minutes + " " + hours + " " + day + " " + month + " ?";
                dates = testCronExpression(cronExpression);
            }
            
            if (dates != null) {
        		cronExpression = cronExpression.replace('?', '*');
        		cronExpression = cronExpression.substring(2);
            	request.setAttribute(REQ_CRON_EXPRESSION, cronExpression);
            	request.setAttribute(REQ_CRON_DATES, dates);
            	request.setAttribute(REQ_JOB_NAME, jobName);
            	return "cronValidation";
            }
    		
    	} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.cronValidation()" + FrameworkUtil.getStackTraceAsString(e));
    		}
//    		addActionError(getText("Cron Expression failed to validate"));
    	}
    	return "cronValidation";
    }
    
    public Date[] testCronExpression(String expression)throws ParseException{
	    Date[] dates = null;
	    try {
	    	if (debugEnabled) {
	    		S_LOGGER.debug("Entering Method  CI.testCronExpression(String expression)");
			}
	    	if (debugEnabled) {
	    		S_LOGGER.debug("testCronExpression() Expression = "+expression);
			}
	        final CronExpression cronExpression = new CronExpression(expression);
	        final Date nextValidDate1 = cronExpression.getNextValidTimeAfter(new Date());
	        final Date nextValidDate2 = cronExpression.getNextValidTimeAfter(nextValidDate1);
	        final Date nextValidDate3 = cronExpression.getNextValidTimeAfter(nextValidDate2);
	        final Date nextValidDate4 = cronExpression.getNextValidTimeAfter(nextValidDate3);
	        dates = new Date[]{nextValidDate1,nextValidDate2,nextValidDate3,nextValidDate4};
	    } catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.testCronExpression()" + FrameworkUtil.getStackTraceAsString(e));
    		}
	    }
	    return dates;
    }
    
	public static String getPortNo(String path)  throws Exception {
		if (debugEnabled) {
			S_LOGGER.debug("Entering Method CI.getPortNo()");
        }
		String portNo = "";
		try {
			Document document = ApplicationsUtil.getDocument(new File(path + File.separator +"pom.xml"));
			String portNoNode = "/project/build/plugins/plugin/configuration/tomcatHttpPort";
			NodeList nodelist = org.apache.xpath.XPathAPI.selectNodeList(document, portNoNode);
			portNo = nodelist.item(0).getTextContent();
		} catch (Exception e) {
        	if (debugEnabled) {
        		S_LOGGER.error("Entered into catch block of CI.getPortNo()" + FrameworkUtil.getStackTraceAsString(e));
    		}
		}
		return portNo;
	}
	
    public String CIBuildDownload() {
		if (debugEnabled) {
            S_LOGGER.debug("Entering Method CI.CIBuildDownload()");
        }
    	try {
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
    		Project project = administrator.getProject(projectCode);
    		CIJob existJob = administrator.getJob(project);
            //Get it from web path
            URL url = new URL(buildDownloadUrl);
            fileInputStream = url.openStream();
			fileName = existJob.getName();
			return SUCCESS;
		} catch (Exception e) {
        	if (debugEnabled) {
                S_LOGGER.error("Entered into catch block of CI.CIBuildDownload()" + FrameworkUtil.getStackTraceAsString(e));
    		}
		}
		return SUCCESS;
    }
    
    public String buildProgress() {
    	try {
    		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
    		Project project = administrator.getProject(projectCode);
            CIJob existJob = administrator.getJob(project);
    		if(existJob != null) {
    			boolean buildJenkinsAlive = false;
    			buildJenkinsAlive = DiagnoseUtil.isConnectionAlive("http", existJob.getJenkinsUrl(), Integer.parseInt(existJob.getJenkinsPort()));
    	    	if (debugEnabled) {
    				S_LOGGER.debug("Build jenkins alive " + buildJenkinsAlive);
    			}
    			if (buildJenkinsAlive == true && administrator.getProgressInBuild(project) > 0) {
                	buildInProgress = true;
                }
    		}
		} catch (Exception e) {
        	if (debugEnabled) {
                S_LOGGER.error("Entered into catch block of CI.buildProgress()" + FrameworkUtil.getStackTraceAsString(e));
    		}
		}
		return SUCCESS;
    }
    
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSvnurl() {
		return svnurl;
	}

	public void setSvnurl(String svnurl) {
		this.svnurl = svnurl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getEmails() {
		return emails;
	}

	public void setEmails(String[] email) {
		this.emails = email;
	}

	public String getSuccessEmailIds() {
		return successEmailIds;
	}

	public void setSuccessEmailIds(String successEmailId) {
		this.successEmailIds = successEmailId;
	}

	public String getFailureEmailIds() {
		return failureEmailIds;
	}

	public void setFailureEmailIds(String failureEmailId) {
		this.failureEmailIds = failureEmailId;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public String getShowSettings() {
		return showSettings;
	}

    public void setShowSettings(String showSettings) {
    	this.showSettings = showSettings;
    }

    public List<String> getServerSettings() {
    	return serverSettings;
    }

    public void setServerSettings(List<String> serverSettings) {
    	this.serverSettings = serverSettings;
    }

    public List<String> getDbSettings() {
    	return dbSettings;
    }

    public void setDbSettings(List<String> dbSettings) {
    	this.dbSettings = dbSettings;
    }

    public List<String> getWebsrvcSettings() {
    	return websrvcSettings;
    }

    public void setWebsrvcSettings(List<String> websrvcSettings) {
    	this.websrvcSettings = websrvcSettings;
    }

    public List<String> getEmailSettings() {
    	return emailSettings;
    }

    public void setEmailSettings(List<String> emailSettings) {
    	this.emailSettings = emailSettings;
    }

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebservice() {
		return webservice;
	}

	public void setWebservice(String webservice) {
		this.webservice = webservice;
	}

	public String getBuildDownloadUrl() {
		return buildDownloadUrl;
	}

	public void setBuildDownloadUrl(String buildDownloadUrl) {
		this.buildDownloadUrl = buildDownloadUrl;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSenderEmailId() {
		return senderEmailId;
	}

	public void setSenderEmailId(String senderEmailId) {
		this.senderEmailId = senderEmailId;
	}

	public String getSenderEmailPassword() {
		return senderEmailPassword;
	}

	public void setSenderEmailPassword(String senderEmailPassword) {
		this.senderEmailPassword = senderEmailPassword;
	}

	public int getTotalBuildSize() {
		return totalBuildSize;
	}

	public void setTotalBuildSize(int totalBuildSize) {
		this.totalBuildSize = totalBuildSize;
	}

	public boolean isBuildInProgress() {
		return buildInProgress;
	}

	public void setBuildInProgress(boolean buildInProgress) {
		this.buildInProgress = buildInProgress;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getSdk() {
		return sdk;
	}

	public void setSdk(String sdk) {
		this.sdk = sdk;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getProguard() {
		return proguard;
	}

	public void setProguard(String proguard) {
		this.proguard = proguard;
	}

	public List<String> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<String> triggers) {
		this.triggers = triggers;
	}
}
