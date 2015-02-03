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
	 * ʡ���б�
	 */
	private List<Province> provinceList;
	/**
	 * �м��б�
	 */
	private List<City> cityList;
	/**
	 * �ؼ��б�
	 */
	private List<County> countyList;
	/**
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	/**
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	/**
	 * ��ǰѡ�еļ���
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
	 * @Description:��ѯȫ�����е�ʡ�ݣ����ȴ����ݿ��ѯ�����û�в�ѯ����ȥ�������ϲ�ѯ
	 * @author: ikgwr
	 * @time:2015-2-3 ����4:21:50
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
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}

	/**
	 * 
	 * @Description:��ѯѡ��ʡ�����е��У����ȴ����ݿ��ѯ�������ѯ������ȥ��������ѯ
	 * @author: ikgwr
	 * @time:2015-2-3 ����4:28:57
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
	 * @Description:��ѯѡ���������е��أ����ȴ����ݿ��ѯ�������ѯ�����ٴӷ������ϲ�ѯ
	 * @author: ikgwr
	 * @time:2015-2-3 ����9:09:59
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
	 * @Description:���ݴ���Ĵ��ź����ʹӷ�������ѯʡ��������
	 * @param code
	 * @param type
	 * @author: ikgwr
	 * @time:2015-2-3 ����9:15:36
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
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

	}

	/**
	 * 
	 * @Description:��ʾ���ȶԻ���
	 * @author: ikgwr
	 * @time:2015-2-3 ����4:18:37
	 */
	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCancelable(false);
		}
		progressDialog.show();
	}

	/**
	 * 
	 * @Description:�رս��ȶԻ���
	 * @author: ikgwr
	 * @time:2015-2-3 ����4:19:40
	 */
	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	/**
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷����м��б�ʡ���б�����ֱ���˳�
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
