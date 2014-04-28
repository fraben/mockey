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

//Taken from:
//http://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/
package com.accia77.mockey.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.accia77.mockey.R;
import com.accia77.mockey.model.NavDrawerItem;

public class NavDrawerListAdapter extends BaseAdapter {
    
    private Context context;
    private ArrayList<NavDrawerItem> navDrawerItems;
     
    public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }
 
    @Override
    public int getCount() {
        return navDrawerItems.size();
    }
 
    @Override
    public Object getItem(int position) {       
        return navDrawerItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }
          
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
          
        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());        
        txtTitle.setText(navDrawerItems.get(position).getTitle());
         
        return convertView;
    }
 
}