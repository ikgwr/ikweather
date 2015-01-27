package com.ikgwr.ikweather.app.db;

import java.util.ArrayList;
import java.util.List;

import com.ikgwr.ikweather.app.model.City;
import com.ikgwr.ikweather.app.model.County;
import com.ikgwr.ikweather.app.model.Province;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class IKWeatherDB {
	/**
	 * 数据库名
	 */
	public static final String DB_NAME = "ik_weather";
	/**
	 * 数据库版本
	 */
	public static final int VERSION = 1;
	private static IKWeatherDB ikWeatherDB;
	private SQLiteDatabase db;

	/**
	 * 将构造方法私有化
	 */
	private IKWeatherDB(Context context) {
		IKWeatherOpenHelper ikWeatherOpenHelper = new IKWeatherOpenHelper(
				context, DB_NAME, null, VERSION);
		db = ikWeatherOpenHelper.getWritableDatabase();

	}

	/**
	 * 获取IKWeatherDB的实例
	 */
	public synchronized static IKWeatherDB getInstance(Context context) {
		if (ikWeatherDB == null) {
			ikWeatherDB = new IKWeatherDB(context);
		}
		return ikWeatherDB;
	}

	/**
	 * 将Province实例存储到数据库
	 */
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvince_name());
			values.put("province_code", province.getProvince_code());
			db.insert("Province", null, values);
		}
	}

	/**
	 * 从数据库读取全国所有的省份信息
	 */
	public List<Province> loadProvinces() {
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db
				.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {

				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvince_name(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvince_code(cursor.getString(cursor
						.getColumnIndex("province_code")));
				list.add(province);
			} while (cursor.moveToNext());
		}
		return list;
	}

	/**
	 * 将City实例存储到数据库
	 */
	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			db.insert("City", null, values);
		}
	}

	/**
	 * 从数据库读取某省下所有的城市信息
	 */
	public List<City> loadCity() {
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				list.add(city);
			} while (cursor.moveToNext());
		}
		return list;
	}

	/**
	 * 将County实例存储到数据库
	 */
	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			db.insert("County", null, values);
		}
	}

	/**
	 * 从数据库读取某城市下所有的县信息
	 */
	public List<County> loadCounty() {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setCityId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				list.add(county);
			} while (cursor.moveToNext());
		}
		return list;
	}
}
