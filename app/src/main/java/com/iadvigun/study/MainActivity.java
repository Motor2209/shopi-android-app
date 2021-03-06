package com.iadvigun.study;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iadvigun.study.utils.AlarmApi;
import com.iadvigun.study.utils.ProductAPI;
import com.iadvigun.study.utils.ShopApi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private Button buttonAlarm;
    private Button buttonProd;
    private Button buttonShop;
    private Button buttonAddProduct;
    private Button buttonAddShop;
    private TextView textResult;
    private RecyclerView recyclerView;
    private AdapterProduct adapterProduct;
    private AdapterShop adapterShop;
    private ImageView shopiLogo;
    private TextView alarmMessage;
    private TextView coordinates;
    private List<Product> productListFromDB = new ArrayList<>();
    private List<Shop> shopListFromDB = new ArrayList<>();
    private List<Product> alarmListFromDB = new ArrayList<>();
    private Product addedProduct;
    private Shop addedShop;
    private double actualLatitude = 0;
    private double actualLongitude = 0;
    private Shop nearestShop;
    private SharedPreferences sPref;
    private NotificationManager notificationManager;
    private int NOTIFY_ID = 1;
    private String ChannelId = "channel_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonAlarm = findViewById(R.id.search_button);
        buttonProd = findViewById(R.id.button2);
        buttonShop = findViewById(R.id.button3);
        shopiLogo = findViewById(R.id.imageShopiMain);
        textResult = findViewById(R.id.textView2);  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        buttonAddProduct = findViewById(R.id.button_additionProduct);
        buttonAddShop = findViewById(R.id.button_additionShop);
        alarmMessage = findViewById(R.id.tv_ALARM_Message);
        coordinates = findViewById(R.id.textCoord);

        createRecyclerView();

        View.OnClickListener onClickListenerAlarms = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonAlarm.setBackgroundColor(getResources().getColor(R.color.buttonOn));
                buttonProd.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                buttonShop.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                shopiLogo.setVisibility(View.INVISIBLE);
                buttonAddProduct.setVisibility(View.INVISIBLE);
                buttonAddShop.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                loadAlarmsFromPhoneMemory();
                adapterProduct.clearItems();
                adapterProduct.setAlarmOrListPr(1);
                adapterProduct.setItems(alarmListFromDB);
                recyclerView.setAdapter(adapterProduct);

            }
        };
        buttonAlarm.setOnClickListener(onClickListenerAlarms);


        View.OnClickListener onClickListenerProd = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonProd.setBackgroundColor(getResources().getColor(R.color.buttonOn));
                buttonShop.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                buttonAlarm.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                shopiLogo.setVisibility(View.INVISIBLE);
                buttonAddShop.setVisibility(View.INVISIBLE);
                buttonAddProduct.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                loadProductsFromPhoneMemory();
                adapterProduct.clearItems();
                adapterProduct.setAlarmOrListPr(2);
                adapterProduct.setItems(productListFromDB);
                recyclerView.setAdapter(adapterProduct);


            }
        };
        buttonProd.setOnClickListener(onClickListenerProd);


        View.OnClickListener onClickListenerAddProd = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "You clicked on button Add Product",
                        Toast.LENGTH_SHORT).show();

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_product_layout);
                dialog.setTitle("Add product");

                final EditText editTextName = dialog.findViewById(R.id.edit_name);
                final EditText editTextAmount = dialog.findViewById(R.id.edit_amount);
                final EditText editTextExpiration = dialog.findViewById(R.id.edit_expiration);

                Button dialogButtonAccept = (Button) dialog.findViewById(R.id.button_accept);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.button_cancel);
                Button dialogButtonPlusAmount = (Button) dialog.findViewById((R.id.button_plus_Amount));
                Button dialogButtonPlusExpiration = (Button) dialog.findViewById((R.id.button_plus_Expiration));
                Button dialogButtonMinusAmount = (Button) dialog.findViewById((R.id.button_minus_Amount));
                Button dialogButtonMinusExpiration = (Button) dialog.findViewById((R.id.button_minus_Expiration));
                Button dialogClose = (Button) dialog.findViewById(R.id.button_close);



                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialogClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialogButtonPlusAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputedAmount = editTextAmount.getText().toString();
                        if (inputedAmount.equals("")) {
                            inputedAmount = "0";
                        }
                        int inputedAm = Integer.parseInt(inputedAmount);
                        if (inputedAm < 0) {
                            inputedAm = 0;
                        }
                        inputedAm++;
                        editTextAmount.setText(String.valueOf(inputedAm));

                    }
                });

                dialogButtonPlusExpiration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputedExpiration = editTextExpiration.getText().toString();
                        if (inputedExpiration.equals("")) {
                            inputedExpiration = "0";
                        }
                        int inputedExp = Integer.parseInt(inputedExpiration);
                        if (inputedExp < 0) {
                            inputedExp = 0;
                        }
                        inputedExp++;
                        editTextExpiration.setText(String.valueOf(inputedExp));
                    }
                });

                dialogButtonMinusAmount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputedAmount = editTextAmount.getText().toString();
                        if (inputedAmount.equals("")) {
                            inputedAmount = "0";
                        }
                        int inputedAm = Integer.parseInt(inputedAmount);
                        if (inputedAm <= 0) {
                            inputedAm = 0;
                        } else {
                            inputedAm--;
                        }
                        editTextAmount.setText(String.valueOf(inputedAm));

                    }
                });

                dialogButtonMinusExpiration.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputedExpiration = editTextExpiration.getText().toString();
                        if (inputedExpiration.equals("")) {
                            inputedExpiration = "0";
                        }
                        int inputedExp = Integer.parseInt(inputedExpiration);
                        if (inputedExp <= 0) {
                            inputedExp = 0;
                        } else {
                            inputedExp--;
                        }
                        editTextExpiration.setText(String.valueOf(inputedExp));
                    }
                });


                dialogButtonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputtedName = editTextName.getText().toString();
                        String inputtedAmount = editTextAmount.getText().toString();
                        if (inputtedAmount.equals("")) {
                            inputtedAmount = "0";
                        }
                        String inputtedExpiration = editTextExpiration.getText().toString();
                        if (inputtedExpiration.equals("")) {
                            inputtedExpiration = "0";
                        }

                        addedProduct = new Product(inputtedName, Integer.parseInt(inputtedAmount),
                                Integer.parseInt(inputtedExpiration));

                        addProductToDB();
                        getProductDataFromDB();
                        recyclerView.setAdapter(adapterProduct);
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Added new product",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.show();
                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }
        };
        buttonAddProduct.setOnClickListener(onClickListenerAddProd);


        View.OnClickListener onClickListenerShop = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonShop.setBackgroundColor(getResources().getColor(R.color.buttonOn));
                buttonProd.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                buttonAlarm.setBackgroundColor(getResources().getColor(R.color.buttonOf));
                shopiLogo.setVisibility(View.INVISIBLE);
                buttonAddProduct.setVisibility(View.INVISIBLE);
                buttonAddShop.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                // adapterProduct.clearItems();
                loadShopsFromPhoneMemory();
                adapterShop.clearItems();
                recyclerView.setAdapter(adapterShop);
                adapterShop.setItems(shopListFromDB);
                recyclerView.setAdapter(adapterShop);

            }
        };
        buttonShop.setOnClickListener(onClickListenerShop);

        buttonAddShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_shop_layout);
                dialog.setTitle("Add shop");

                final EditText editTextName = dialog.findViewById(R.id.edit_name_shop);
                final EditText editTextLatitude = dialog.findViewById(R.id.edit_latitude);
                final EditText editTextLongitude = dialog.findViewById(R.id.edit_longitude);
                final EditText editTextAreaSize = dialog.findViewById(R.id.edit_area_Size);

                Button dialogButtonAccept = (Button) dialog.findViewById(R.id.button_accept_shop);
                Button dialogButtonCancel = (Button) dialog.findViewById(R.id.button_cancel_shop);
                Button dialogClose = (Button) dialog.findViewById(R.id.button_close_shop);

                dialog.show();

                dialogClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });

                dialogButtonAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Addition method",
                                Toast.LENGTH_SHORT).show();
                        String inputedName = editTextName.getText().toString();
                        String inputedLatitude = editTextLatitude.getText().toString();
                        if (inputedLatitude.equals("")) {
                            inputedLatitude = "0";
                        }
                        String inputedLongitude = editTextLongitude.getText().toString();
                        if (inputedLongitude.equals("")) {
                            inputedLongitude = "0";
                        }
                        String inputedAreaSize = editTextAreaSize.getText().toString();
                        if (inputedAreaSize.equals("")) {
                            inputedAreaSize = "0";
                        }

                        addedShop = new Shop(inputedName, Float.parseFloat(inputedLatitude),
                                Float.parseFloat(inputedLongitude), Integer.parseInt(inputedAreaSize));
                        addShopToDB();
                        getShopDataFromDB();
                        recyclerView.setAdapter(adapterShop);
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Added new shop",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
            }
        });

        LocationManager location = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);

        loadAlarmsFromPhoneMemory();
        loadShopsFromPhoneMemory();
        loadProductsFromPhoneMemory();
        updateAllDataFromDB();
        makeAlarmNearTheShop();
        DBupdateScannerEditedProduct();
        DBupdateScannerDeletedProduct();
        DBupdateScannerDeletedShop();
        DBupdateScannerEditedShop();

    }
/*
public void createChannelIfNeeded(NotificationManager manager){
  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
      NotificationChannel notificationChannel = new NotificationChannel(ChannelId, ChannelId, NotificationManager.IMPORTANCE_DEFAULT);
      manager.createNotificationChannel(notificationChannel);

  }

}

public void createNotificationMessage(){
    notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
            intent, PendingIntent.FLAG_UPDATE_CURRENT);
    NotificationCompat.Builder  notBilder =
            new NotificationCompat.Builder(getApplicationContext(), ChannelId )
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pendingIntent)
                    .setContentTitle("Hello, from SHOPI! ")
                    .setContentText("You are near the shop " + nearestShop.getName() + "! "
                            + "You can by your " + productListFromDB.size() + " overdue products here :)")
                    .setPriority(PRIORITY_HIGH);
    createChannelIfNeeded(notificationManager);
    notificationManager.notify(NOTIFY_ID, notBilder.build());
}*/

    public void initLocationManager(){

    }

    public void makeAlarmNearTheShop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while (true) {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(alarmListFromDB.size() == 0 && count == 0 ){
                        try {
                            Thread.sleep(60000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count++;
                    }else if (alarmListFromDB.size() == 0 && count == 1 )
                    {
                        try {
                            Thread.sleep(600000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if ( AreYouNearTheShop()) {

                        alarmMessage.setText("You are near the shop " + nearestShop.getName() + "!\n"
                                + "You can by your " + alarmListFromDB.size() +
                                " overdue products here :)");
                        try {
                            Thread.sleep(7000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        alarmMessage.setText("");
                        try {
                            Thread.sleep(6000000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    public boolean AreYouNearTheShop() {

        if (theSmallestDistanceToShop() < Float.MAX_VALUE && theSmallestDistanceToShop() < (float) 1000 +
                ((float)nearestShop.getAreaSize()/2)) {
            return true;
        } else
            return false;
    }


    public float theSmallestDistanceToShop() {
        float theSmallestDistance = Float.MAX_VALUE;
        if (shopListFromDB.size() > 0) {
            float[] result = new float[3];
            for (int i = 0; i < shopListFromDB.size(); i++) {
                Location.distanceBetween(actualLatitude, actualLongitude, shopListFromDB.get(i).getLatitude(),
                        shopListFromDB.get(i).getLongitude(), result);
                if (theSmallestDistance > result[0]) {
                    nearestShop = shopListFromDB.get(i);
                    theSmallestDistance = result[0];
                }
            }
        }
        return theSmallestDistance;
    }

    public void getAlarmDataFromDB() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AlarmApi alarmAPI = retrofit.create(AlarmApi.class);
        Call<List<Product>> call = alarmAPI.getAlarms();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Can t load alarms! ");
                    return;
                }
                List<Product> list = response.body();
                alarmListFromDB.clear();
                alarmListFromDB.addAll(list);
                saveAlarmsToPhoneMemory();

                textResult.setText("");
                for (Product product : list) {
                    Product product1 = product;
                    // String s = product1.getId().toString();
                    String content = "";
                    content += " Alarm: " + product.getId(); /*+ " amount: " + product.getAmount() +
                    " expiration: " + product.getExpiration() +" id: " + s + "\n";*/

                    textResult.append(content);
                }

            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                textResult.setText(t.getMessage());
            }
        });
    }

    void getProductDataFromDB() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ProductAPI productAPI = retrofit.create(ProductAPI.class);
        Call<List<Product>> call = productAPI.getProduct();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Cant Load products!");
                    return;
                }
                List<Product> list = response.body();
                productListFromDB.clear();
                productListFromDB.addAll(list);
              //  adapterProduct.setItems(productListFromDB);
                saveProductsToPhoneMemory();

                textResult.setText("");
                for (Product product : list) {
                    Product product1 = product;
                    String s = product1.getId().toString();
                    String content = "";
                    content += " Product: " + product.getName(); /*+ " amount: " + product.getAmount() +
                    " expiration: " + product.getExpiration() +" id: " + s + "\n";*/
                    textResult.setText(content);
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                textResult.setText(t.getMessage());
            }
        });
    }

    private void addProductToDB() {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ProductAPI productApi = retrofit.create(ProductAPI.class);

        Call<Product> call = productApi.createPost(addedProduct);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Code" + response.code());
                    return;
                }
                Product responsePr = response.body();


                textResult.setText("response: " + response.code());
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                textResult.setText(t.getMessage());

            }
        });
    }

    public void upDateProductInDB() {
        Product editedProduct = adapterProduct.getEditedProduct();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ProductAPI productApi = retrofit.create(ProductAPI.class);
        Call<Product> call = productApi.createPUT(editedProduct.getId(), editedProduct);


        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Code" + response.code());
                    return;
                }
                Product responsePr = response.body();
                recyclerView.setAdapter(adapterProduct);
                textResult.setText("response: " + response.code() + responsePr.getName());
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                textResult.setText(t.getMessage());

            }
        });
    }

    public void IsUpdatedProductExist() {
        Product editedProduct = adapterProduct.getEditedProduct();
        if (editedProduct != null) {
            upDateProductInDB();
            adapterProduct.ResetEditedProductForNull();


        }
    }

    public void DBupdateScannerEditedProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    IsUpdatedProductExist();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void deleteProductFromDB() {
        Product deletedPr = adapterProduct.getDeletedProduct();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ProductAPI productApi = retrofit.create(ProductAPI.class);
        Call<Void> call = productApi.deleteProduct(deletedPr.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                textResult.setText(response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                textResult.setText(t.getMessage());
            }
        });

    }

    public void IsDeletedProductExist() {
        Product deletedProduct = adapterProduct.getDeletedProduct();
        if (deletedProduct != null) {
            deleteProductFromDB();
            textResult.setText("DELETED!");
            adapterProduct.ResetDeletedProductForNull();

            // adapterProduct.setItems(listFromDB);
            // recyclerView.setAdapter(adapterProduct);
        }
    }

    public void DBupdateScannerDeletedProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    IsDeletedProductExist();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    void getShopDataFromDB() {
        adapterShop = new AdapterShop(this);
//        recyclerView.setAdapter(adapterShop);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://98e034c082c1.ngrok.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ShopApi shopAPI = retrofit.create(ShopApi.class);
        Call<List<Shop>> call = shopAPI.getShops();
        call.enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Cant load shopsList");
                    return;
                }
                List<Shop> list = response.body();
                shopListFromDB.clear();
                shopListFromDB.addAll(list);
                saveShopsToPhoneMemory();

                textResult.setText("");
                for (Shop shop : list) {
                    Shop product1 = shop;
                    String s = product1.getId().toString();
                    String content = "";
                    content += " shops: " + shop.getName(); /*+ " amount: " + product.getAmount() +
                    " expiration: " + product.getExpiration() +" id: " + s + "\n";*/
                    textResult.append(content);
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                textResult.setText(t.getMessage());
            }
        });
    }

    private void addShopToDB() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ShopApi shopApi = retrofit.create(ShopApi.class);
        Call<Shop> call = shopApi.createPost(addedShop);
        call.enqueue(new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Code" + response.code());
                    return;
                }
                Shop responseShop = response.body();


                textResult.setText("response: " + response.code());
            }

            @Override
            public void onFailure(Call<Shop> call, Throwable t) {
                textResult.setText(t.getMessage());

            }
        });
    }

    public void updateShopInDB() {
        Shop editedShop = adapterShop.getEditedShop();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ShopApi shopApi = retrofit.create(ShopApi.class);
        Call<Shop> call = shopApi.createPUT(editedShop.getId(), editedShop);


        call.enqueue(new Callback<Shop>() {
            @Override
            public void onResponse(Call<Shop> call, Response<Shop> response) {
                if (!response.isSuccessful()) {
                    textResult.setText("Code" + response.code());
                    return;
                }
                Shop responseShop = response.body();
                // recyclerView.setAdapter(adapterShop);
                textResult.setText("response: " + response.code() + responseShop.getName());
            }

            @Override
            public void onFailure(Call<Shop> call, Throwable t) {
                textResult.setText(t.getMessage());

            }
        });
    }

    public void IsUpdatedShopExist() {
        Shop editedShop = adapterShop.getEditedShop();
        if (editedShop != null) {
            updateShopInDB();
            adapterShop.resetEditedShopForNull();


        }
    }

    public void DBupdateScannerEditedShop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    IsUpdatedShopExist();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void deleteShopFromDB() {
        Shop deletedPr = adapterShop.getDeletedShop();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.baseURL))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ShopApi shopApi = retrofit.create(ShopApi.class);
        Call<Void> call = shopApi.deleteProduct(deletedPr.getId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
//                textResult.setText(response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                textResult.setText(t.getMessage());
            }
        });

    }

    public void IsDeletedShopExist() {
        Shop deletedShop = adapterShop.getDeletedShop();
        if (deletedShop != null) {
            deleteShopFromDB();
            textResult.setText("DELETED!");
            adapterShop.resetDeletedShopForNull();

            // adapterProduct.setItems(listFromDB);
            // recyclerView.setAdapter(adapterShop);
        }
    }

    public void DBupdateScannerDeletedShop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    IsDeletedShopExist();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onLocationChanged(Location location) {
        actualLongitude = location.getLongitude();
        actualLatitude = location.getLatitude();
        String coordinatess = "long: " + String.valueOf(actualLongitude) + " lat: " + String.valueOf(actualLatitude);
        coordinates.setText(coordinatess);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*public void makeToastForAlarm() {

        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, "Hey, you are near: " +
                                nearestShop.getName() + "\n" +
                                "you can by your " + productListFromDB.size() +
                                " overdue product(s)" + "\n" + "from Alarms here :)",
                        Toast.LENGTH_LONG).show();
            }
        });
    }*/

    public void createRecyclerView() {
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager); // отображение списка тут линеар
        recyclerView.setHasFixedSize(true);
        recyclerView.setVisibility(View.INVISIBLE);
        adapterProduct = new AdapterProduct(this);
        recyclerView.setAdapter(adapterProduct);
        adapterShop = new AdapterShop(this);
    }

    public void saveProductsToPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(productListFromDB);
        editor.putString("productList", json);
        editor.apply();
        Toast.makeText(MainActivity.this, "saves products to phone",
                Toast.LENGTH_SHORT).show();

    }

    public void loadProductsFromPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        Gson gsonLoad = new Gson();
        String jsonLoad = sPref.getString("productList", null);
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        productListFromDB = gsonLoad.fromJson(jsonLoad, type);

        if (productListFromDB == null) {
            productListFromDB = new ArrayList<>();
        }
    }

    public void saveAlarmsToPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(alarmListFromDB);
        editor.putString("alarmList", json);
        editor.apply();
    }

    public void loadAlarmsFromPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        Gson gsonLoad = new Gson();
        String jsonLoad = sPref.getString("alarmList", null);
        Type type = new TypeToken<ArrayList<Product>>() {
        }.getType();
        alarmListFromDB = gsonLoad.fromJson(jsonLoad, type);

        if (alarmListFromDB == null) {
            alarmListFromDB = new ArrayList<>();
        }
    }

    public void saveShopsToPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shopListFromDB);
        editor.putString("shopList", json);
        editor.apply();
        Toast.makeText(MainActivity.this, "saved shops to Phone!",
                Toast.LENGTH_SHORT).show();
    }

    public void loadShopsFromPhoneMemory() {
        sPref = getPreferences(MODE_PRIVATE);
        Gson gsonLoad = new Gson();
        String jsonLoad = sPref.getString("shopList", null);
        Type type = new TypeToken<ArrayList<Shop>>() {
        }.getType();
        shopListFromDB = gsonLoad.fromJson(jsonLoad, type);

        if (shopListFromDB == null) {
            shopListFromDB = new ArrayList<>();
            Toast.makeText(MainActivity.this, "loaded null list",
                    Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(MainActivity.this, "loaded list correctly",
                Toast.LENGTH_SHORT).show();
    }

    public void updateAllDataFromDB(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                getAlarmDataFromDB();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getProductDataFromDB();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getShopDataFromDB();
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                 }
               }
            }
        }).start();
    }
}