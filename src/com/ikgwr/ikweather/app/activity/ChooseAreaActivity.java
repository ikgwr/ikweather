package com.ikgwr.ikweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ikgwr.ikweather.app.R;
import com.ikgwr.ikweather.app.db.IKWeatherDB;
import com.ikgwr.ikweather.app.model.City;
import com.ikgwr.ikweather.app.model.County;
import com.ikgwr.ikweather.app.model.Province;
import com.ikgwr.ikweather.app.util.HttpCallbackListener;
import com.ikgwr.ikweather.app.util.HttpUtil;
import com.ikgwr.ikweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	private ProgressDialog progressDialog;
	private ListView listView;
	private TextView titleText;
	private ArrayAdapter<String> adapter;
	private IKWeatherDB ikWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	/**
	 * 省级列表
	 */
	private List<Province> provinceList;
	/**
	 * 市级列表
	 */
	private List<City> cityList;
	/**
	 * 县级列表
	 */
	private List<County> countyList;
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	/**
	 * 选中的城市
	 */
	private City selectedCity;
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	private static final int LEVEL_PROVINCE = 0;
	private static final int LEVEL_CITY = 1;
	private static final int LEVEL_COUNTY = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) this.findViewById(R.id.list_view);
		titleText = (TextView) this.findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		ikWeatherDB = IKWeatherDB.getInstance(this);
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
				}
			}

		});
		queryProvinces();

	}

	/**
	 * 
	 * @Description:查询全国所有的省份，优先从数据库查询，如果没有查询到再去服务器上查询
	 * @author: ikgwr
	 * @time:2015-2-3 下午4:21:50
	 */
	private void queryProvinces() {
		provinceList = ikWeatherDB.loadProvinces();
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
	 * 
	 * @Description:查询选中省内所有的市，优先从数据库查询，如果查询不到再去服务器查询
	 * @author: ikgwr
	 * @time:2015-2-3 下午4:28:57
	 */
	private void queryCities() {
		cityList = ikWeatherDB.loadCity(selectedProvince.getId());
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

	/**
	 * 
	 * @Description:查询选中市内所有的县，优先从数据库查询，如果查询不到再从服务器上查询
	 * @author: ikgwr
	 * @time:2015-2-3 下午9:09:59
	 */
	private void queryCounties() {
		countyList = ikWeatherDB.loadCounty(selectedCity.getId());
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

	/**
	 * 
	 * @Description:根据传入的代号和类型从服务器查询省市县数据
	 * @param code
	 * @param type
	 * @author: ikgwr
	 * @time:2015-2-3 下午9:15:36
	 */
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
					result = Utility.handleProvinceResponse(ikWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCityResponse(ikWeatherDB, response,
							selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountyResponse(ikWeatherDB,
							response, selectedCity.getId());
				}
				if (result) {
					runOnUiThread(new Runnable() {

						@Override
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

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	/**
	 * 
	 * @Description:显示进度对话框
	 * @author: ikgwr
	 * @time:2015-2-3 下午4:18:37
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCancelable(false);
		}
		progressDialog.show();
	}

	/**
	 * 
	 * @Description:关闭进度对话框
	 * @author: ikgwr
	 * @time:2015-2-3 下午4:19:40
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市级列表、省级列表、还是直接退出
	 */
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();
		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();
		} else {
			finish();
		}
	}
}
