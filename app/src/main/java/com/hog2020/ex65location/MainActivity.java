package com.hog2020.ex65location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    TextView best;
    TextView mylocation;
    TextView tvAutoMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        best = findViewById(R.id.best);
        mylocation = findViewById(R.id.tv_mylocation);
        tvAutoMyLocation= findViewById(R.id.tv_automyloaction);

        //위치 정보 소환
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //디바이스에서 위치정보를 제공하려고 장치 여러개
        //이런것들을 위치정보 제공자(Location Provider) 라고 부름
        //1.gps : 가장정확도 높음, 무료, 실내불가,배터리소모량 높음
        //2.network(wifi,3g,lte) : 중간 정확도, 유료 or 무료 배터리소모량 중간
        //3.passive : 다른앱의 마지막 위치정보를 통해서 위치를 얻어오는 방식, 정확도 가장 낮음, 사용빈도 거의 없음

        //위치정보 제공자 중에 최고의 제공자 판별요청
        //최고지의 제공자를 판별하기 위한 기준(criteria) 객체
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(true);//비용지불을 감수 할지 여부
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);//정확도를 요하는지
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);//배터리 소모량
        criteria.setAltitudeRequired(true);//고도에 대한 위치정보 필요여부

        String bestProvider = locationManager.getBestProvider(criteria, true);
        best.setText(bestProvider);
        //베스트 프로바이더 정보를 얻으려면 위치정보사용에 대한 허가(permission)을 받아야함
        //단 위치정보 제공에 대한 퍼미션은 동적 퍼미션: 앱을 설치 할때 사용자 동의를 얻는게 아니라 사용할 때 동의를 얻는방식
        //마시멜로우(api 23 버전) 부터 동적퍼미션 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션을 받았는지 확인
            int checkSelfPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {//거절 되어있는지 여부
                //사용자에게 퍼미션 허용하도록 할 수 있는 다이얼로그 화면을 보여주기
                //단 이 다이얼로그는 개발자가 만드는게 아니라 이미 이 모양을 가진
                //새로운 엑티비티(다이얼 로그 모양을 가진)를 보여주면 됨
                //액티비티 클래스에 이 퍼미션요청 다이얼로그를 보여주도록 요청하는 메소드가 존재함
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissions, 0);
            }
        }

    }
    //requestPermission()을 통해 보여진 다이얼로그의 선택이 완료되면 자동으로 실현


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "위치제공정보 동의", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "위치정보 사용 불가", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public void clickBtn(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //현재 내 위치 얻어오기
        Location location = null;
        if (locationManager.isProviderEnabled("gps")) {

            location = locationManager.getLastKnownLocation("gps");
        }else if(locationManager.isProviderEnabled("network")){
            location= locationManager.getLastKnownLocation("network");
        }
        if (location==null){
            mylocation.setText("내위치 못찾겠어");
        }else{
            //위도, 경도 알아보기
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();

            mylocation.setText(latitude+","+longitude);
        }
    }


    public void clickBtn2(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //내위치 실시간 열기
        if (locationManager.isProviderEnabled("gps")){
            locationManager.requestLocationUpdates("gps",5000,2,locationListener);
        }else if(locationManager.isProviderEnabled("network")){
            locationManager.requestLocationUpdates("network",5000,2,locationListener);
        }
    }
    //반경에 들어간 적있는가?
    boolean wasEnter=false;
    //멤버변수 위치에 위치정보 갱신을 듣는 리스너
    LocationListener locationListener= new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double latitude= location.getLatitude();
            double longitude= location.getLongitude();
            tvAutoMyLocation.setText(latitude+","+longitude);

            //특정 지점에 들어갔을 때 이벤트 발생
            //왕십리역 좌표: 37.5612919,127.0375806

            //내위치(lat,lng)와 왕십리역 사이의 실제거리(m)
            float[] result = new float[]{3};//거리계산 결과를 저장할 배열객체
            Location.distanceBetween(latitude,longitude,37.5612919,127.0375806,result);

            //result[0]의 두 좌표사이의 m거리가 계산되어 저장되어 있음
            if (result[0]<50){//두 좌표 사이 거리가 50m 이내인가?
                //이벤트를 알리는 다이얼로그 보이기
                if (wasEnter==false){
                    new AlertDialog.Builder(MainActivity.this).setMessage("이벤트 달성").setPositiveButton("ok",null).create().show();
                    wasEnter=true;
                }else {
                    wasEnter=false;
                }
            }
        }
    };

    public void clickBtn3(View view) {
        //내위치 자동갱신 제거
        locationManager.removeUpdates(locationListener);
    }
}