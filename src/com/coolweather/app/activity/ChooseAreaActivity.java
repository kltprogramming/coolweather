package com.coolweather.app.activity;

/*import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.model.City;
import com.coolweather.app.model.CoolWeatherDB;
import com.coolweather.app.model.Country;
import com.coolweather.app.model.Province;


import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;
import com.example.coolweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private List<Country> countryList;
	
	private Province selectedProvince;
	private City selectedCity;
	//private Country selectedCountry;
	
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);
		
		SharedPreferences prefs = PreferenceManager.
				getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false) 
				&& !isFromWeatherActivity){
			Intent intent = new Intent (this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int index, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE){
					selectedProvince =provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCountries();
				}else if(currentLevel ==LEVEL_COUNTRY){
					String countryCode =countryList.get(index).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}
			}
		});
				queryProvinces();
			}

			

			private void queryProvinces() {
				// TODO Auto-generated method stub
				provinceList = coolWeatherDB.loadProvinces();
				if(provinceList.size()>0){
					dataList.clear();
					for(Province province:provinceList){
						dataList.add(province.getProvinceName());
					}
					adapter.notifyDataSetChanged();
					listView.setSelection(0);
					titleText.setText("中国");
					currentLevel = LEVEL_PROVINCE;
				}else{
					queryFormServer(null,"province");
				}
			}
			
			private void queryCities() {
				// TODO Auto-generated method stub
				cityList = coolWeatherDB.loadCities(selectedProvince.getId());
				if(cityList.size()>0){
					dataList.clear();
					for(City city:cityList){
						dataList.add(city.getCityName());
						
					}
					adapter.notifyDataSetChanged();
					listView.setSelection(0);
					titleText.setText(selectedProvince.getProvinceName());
					currentLevel = LEVEL_CITY;										
				}else{
					queryFormServer(selectedProvince.getProvinceCode(),"city");
					
				}
			}

			private void queryCountries() {
				// TODO Auto-generated method stub
				countryList = coolWeatherDB.loadCountries(selectedCity.getId());
				if(countryList.size()>0){
					dataList.clear();
					for(Country country:countryList){
						dataList.add(country.getCountryName());
						
					}
					adapter.notifyDataSetChanged();
					listView.setSelection(0);
					titleText.setText(selectedCity.getCityName());
					currentLevel = LEVEL_COUNTRY;
				}else{
					queryFormServer(selectedCity.getCityCode(),"country");
				}
			}
			
			private void queryFormServer(final String code, final String type){
				String address;
				if(!TextUtils.isEmpty(code)){
					address = "http://www.weather.com.cn/data/list3/city" +
				code + ".xml";
				}else{
					address = "http://www.weather.com.cn/data/list3/city.xml";
				}
				showProgressDialog();
				HttpUtil.sendHttpRequest(address, new HttpCallbackListener(){

					@Override
					public void onFinish(String response) {
						// TODO Auto-generated method stub
						boolean result = false;
						if("province".equals(type)){
							result = Utility.handleProvinceResponse(coolWeatherDB, response);
							
						}else if("city".equals(type)){
							result = Utility.handleCitiesResponse(coolWeatherDB, response, 
									selectedProvince.getId());
						}else if("country".equals(type)){
							result  = Utility.handleCountriesResponse(coolWeatherDB, response, 
									selectedCity.getId());
						}
						if(result){
							runOnUiThread(new Runnable(){

								@Override
								public void run() {
									// TODO Auto-generated method stub
									closeProgressDialog();
									if("province".equals(type)){
										queryProvinces();
									}else if ("city".equals(type)){
										queryCities();
									}else if("country".equals(type)){
										queryCountries();
									}
								}
								
							});
						}
					}

					@Override
					public void onError(Exception e) {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								closeProgressDialog();
								Toast.makeText(ChooseAreaActivity.this, 
										"加载失败",Toast.LENGTH_SHORT).show();
								
							}
							
						});
					}
					
				});
			}



			private void showProgressDialog() {
				// TODO Auto-generated method stub
				if(progressDialog == null){
					progressDialog = new ProgressDialog(this);
					progressDialog.setMessage("正在加载...");
					progressDialog.setCanceledOnTouchOutside(false);
					
				}
				progressDialog.show();
			}
			
			private void closeProgressDialog(){
				if (progressDialog!=null){
					progressDialog.dismiss();
				}
			}
			@Override
			public void onBackPressed(){
				if(currentLevel ==LEVEL_COUNTRY){
					queryCities();
					
				}else if (currentLevel == LEVEL_CITY){
					queryProvinces();
				}else{
					if(isFromWeatherActivity){
						Intent intent = new Intent(this,WeatherActivity.class);
						startActivity(intent);
					}
					finish();
				}
			}
			
			
		
		
		
	

}*/

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;
import com.coolweather.app.db.CoolWeatherDB;
import com.coolweather.app.model.City;
import com.coolweather.app.model.County;
import com.coolweather.app.model.Province;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.R.array;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public final static int LEVEL_PROVINCE = 0;
	public final static int LEVEL_CITY = 1;
	public final static int LEVEL_COUNTY = 2;
	// 是否从WeatherActivity中跳过来
	private boolean isFromWeatherActivity;

	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();

	/*
	 * 省、市、县列表
	 */
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	/*
	 * 当前选中的省、市、县
	 */
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	/*
	 * 当前选中的级别
	 */
	private int currentLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = coolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					queryCounties();
				} else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position)
							.getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}

			}

		});
		queryProvinces();
	}

	/*
	 * 查询全国所有省，优先从数据库查询，如果没有查询到再去服务器上查询
	 */
	private void queryProvinces() {
		provinceList = coolWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
	 */
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	private void queryCounties() {
		countyList = coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(final String code, final String type) {
		String address;
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)) {
					result = Utility.handleProvincesResponse(coolWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(coolWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(coolWeatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					// 通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {

						public void run() {
							closeProgressDialog();
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}

						}

					});
				}

			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();

					}
				});

			}
		});
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();

	}

	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

	}

	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			if (isFromWeatherActivity) {
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}

