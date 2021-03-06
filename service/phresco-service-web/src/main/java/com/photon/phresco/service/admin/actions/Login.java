/*
 * ###
 * Service Web Archive
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
package com.photon.phresco.service.admin.actions;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class Login extends ServiceBaseAction {

	private static final long serialVersionUID = -1858839078372821734L;
	private static final Logger S_LOGGER = Logger.getLogger(Login.class);
	private String username = null;
	private String password = null;
	private boolean loginFirst = true;
	
	public String login() {
		S_LOGGER.debug("Entering Method  Login.login()");
		
		if (loginFirst) {
			return LOGIN_RESULT;	
		}
		
		if (validateLogin() && authenticate()) {
			return LOGIN_SUCCESS;
		}
		
		return LOGIN_FAILURE;
	}
	
	private boolean authenticate() {
		S_LOGGER.debug("Entering Method  Login.authenticate()");
		
		/*try {
            Credentials credentials = new Credentials(username, password);
            
            if (userInfo.getDisplayName() == null) {
            	getHttpRequest().setAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN));
                return LOGIN_FAILURE;
            }
            if (!userInfo.isPhrescoEnabled()) {
            	getHttpRequest().setAttribute(REQ_LOGIN_ERROR, getText(ERROR_LOGIN_ACCESS_DENIED));
                return LOGIN_FAILURE;
            } else {
            	loginSuccess(userInfo);
                return LOGIN_SUCCESS;
            }
            
        } catch (Exception e) {
        	if (debugEnabled) {
                S_LOGGER.error("Entered into catch block of Login.login()"+ FrameworkUtil.getStackTraceAsString(e));
    		}
        	getHttpRequest().setAttribute(REQ_LOGIN_ERROR, getText(ERROR_EXCEPTION));
//        	getHttpRequest().setAttribute(REQ_LOGIN_ERROR, e.getLocalizedMessage());
            return LOGIN_FAILURE;
        }*/
		return true;
	}
	
	private boolean validateLogin() {
		S_LOGGER.debug("Entering Method  Login.validateLogin()");
		
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			getHttpRequest().setAttribute(REQ_LOGIN_ERROR, getText(LOGIN_ERROR));
			return false;
		}
		return true;
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

	public boolean isLoginFirst() {
		return loginFirst;
	}

	public void setLoginFirst(boolean loginFirst) {
		this.loginFirst = loginFirst;
	}
	
}
