/**
 * Copyright (c) 2002-2011 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.vagrant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class Shell {

    public static class Result {
        private final int result;
        private final String message;
        private final String cmd;

        Result(int result, String message, String cmd) {
            this.result = result;
            this.cmd = cmd;
            this.message = message;
        }

        public int getExitCode() {
            return result;
        }

        public String getOutput() {
            return message;
        }
        
        public String [] getOutputAsList() {
            return message.split("\n");
        }
        
        public String getCommand() {
            return cmd;
        }
    }

    private File workingDir;
    private Map<String,String> env = new HashMap<String, String>();
    
    public static String streamToString(InputStream in) {
        BufferedReader bre = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = bre.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
                logOutput(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return builder.toString();
    }

    private static void logOutput(String line) {
        System.out.println("[out]: " + line);
    }
    
    public Shell(File workingDir) {
        this.workingDir = workingDir;
    }
    
    public Map<String,String> getEnvironment() {
        return env;
    }
    
    public Result run(String ... cmds) {
        String cmd = StringUtils.join(cmds, " ");
        try {
            Process proc = startProcess(cmd);
            
            String msg = streamToString(proc.getInputStream()) + streamToString(proc.getErrorStream());
            proc.waitFor();

            return new Result(proc.exitValue(), msg, cmd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public Process startProcess(String cmd) {
        try {
            System.out.println(cmd);
            return Runtime.getRuntime().exec(cmd, getEnvironmentParams(), workingDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String[] getEnvironmentParams() {
        String[] envList = new String[env.size()];
        int i=0;
        for(String k : env.keySet()) { 
            envList[i++] = k + "=" + env.get(k);
        }
        return envList;
    }

    public void setWorkingDir(File dir)
    {
        workingDir = dir;
    }

    public File getWorkingDir()
    {
        return workingDir;
    }

}