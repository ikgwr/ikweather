package com.ikgwr.ikweather.app.util;

import android.text.TextUtils;

import com.ikgwr.ikweather.app.db.IKWeatherDB;
import com.ikgwr.ikweather.app.model.City;
import com.ikgwr.ikweather.app.model.County;
import com.ikgwr.ikweather.app.model.Province;

public class Utility {
	/**
	 * 
	 * @Description:解析和处理服务器返回的省级数据
	 * @param ikWeatherDB
	 * @param response
	 *            数据库返回的省级数据
	 * @return boolean
	 * @author: ikgwr
	 * @time:2015-2-2 下午10:38:25
	 */
	public synchronized static boolean handleProvinceResponse(
			IKWeatherDB ikWeatherDB, String response) {
		// 相当于response != null && !response.isEmpty()
		if (!TextUtils.isEmpty(response)) {
			String[] allProvince = response.split(",");
			if (allProvince != null && allProvince.length > 0) {
				for (String p : allProvince) {
					String[] arrary = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(arrary[0]);
					province.setProvinceName(arrary[1]);
					// 将解析出来的数据存储到Province表中
					ikWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @Description:解析和处理服务器返回的市级数据
	 * @param ikWeatherDB
	 * @param response
	 *            服务器返回的市级数据
	 * @return boolean
	 * @author: ikgwr
	 * @time:2015-2-2 下午10:48:10
	 */
	public  static boolean handleCityResponse(
			IKWeatherDB ikWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCity = response.split(",");
			if (allCity != null && allCity.length > 0) {

				for (String c : allCity) {
					String[] arrary = c.split("\\|");
					City city = new City();
					city.setCityCode(arrary[0]);
					city.setCityName(arrary[1]);
					city.setProvinceId(provinceId);
					// 将解析出来 的数据存储到City表中
					ikWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @Description:解析和处理服务器返回的县级数据
	 * @param ikWeatherDB
	 * @param response
	 *            服务器返回的县级数据
	 * @return boolean
	 * @author: ikgwr
	 * @time:2015-2-2 下午11:31:48
	 */
	public  static boolean handleCountyResponse(
			IKWeatherDB ikWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounty = response.split(",");
			if (allCounty != null && allCounty.length > 0) {
				for (String cou : allCounty) {
					String[] arrary = cou.split("\\|");
					County county = new County();
					county.setCountyCode(arrary[0]);
					county.setCountyName(arrary[1]);
					county.setCityId(cityId);
					// 将解析出来的数据存储到County表中
					ikWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
}
