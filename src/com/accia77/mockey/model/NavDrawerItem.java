/*
 * Copyright (C) 2014 fraben 
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
 */

package com.accia77.mockey.model;

//Model class for the Navigation Drawer items
public class NavDrawerItem {
    
    private String title;
    private int icon;
     
    public NavDrawerItem(){}
 
    public NavDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }
     
    public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
        this.title = title;
        this.icon = icon;
    }
     
    public String getTitle(){
        return this.title;
    }
     
    public int getIcon(){
        return this.icon;
    }
        
    public void setTitle(String title){
        this.title = title;
    }
     
    public void setIcon(int icon){
        this.icon = icon;
    }
}