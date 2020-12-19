package com.droideve.apps.nearbystores.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.droideve.apps.nearbystores.GPS.GPStracker;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.appconfig.AppContext;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.databinding.V2FragmentProfileEditBinding;
import com.droideve.apps.nearbystores.helper.CommunFunctions;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.UserParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.ImageUtils;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.Translator;
import com.droideve.apps.nearbystores.utils.Utils;
import com.droideve.apps.nearbystores.views.CustomDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.fragments.ProfileFragment.setup_background_img;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements ImageUtils.PrepareImagesData.OnCompressListner {


    @BindView(R.id.login)
    TextInputEditText loginTxt;

    @BindView(R.id.full_name)
    TextInputEditText fullNameTxt;

    @BindView(R.id.email)
    TextInputEditText emailTxt;

    @BindView(R.id.saveBtn)
    MaterialRippleLayout saveBtn;


    @BindView(R.id.takePicture)
    ImageView takePicture;

    @BindView(R.id.userimage)
    CircularImageView userimage;

    @BindView(R.id.auth_bg)
    ImageView authBgImg;


    private User mUser;

    private ProgressDialog mPdialog;

    private GPStracker gps;

    private RequestQueue queue;

    private CustomDialog mDialogError;

    private int GALLERY_REQUEST = 103;
    private Uri imageToUpload = null;
    private String loadedImageId = "";


    // newInstance constructor for creating fragment with arguments
    public static EditProfileFragment newInstance(int page, String title) {
        EditProfileFragment fragmentFirst = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        V2FragmentProfileEditBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.v2_fragment_profile_edit, container, false);
        View view = binding.getRoot();
        ButterKnife.bind(this, view);


        //button listners
        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFromGallery();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSave();
            }
        });

        //Load data from profile user to the view presentation
        loadProfileData();

        //set random background image
        setup_background_img(view.getContext(), authBgImg);

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        gps = new GPStracker(this.getActivity());
        queue = VolleySingleton.getInstance(getActivity()).getRequestQueue();


    }

    protected void getFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }


    private void loadProfileData() {

        //Make sure that the user is connected before executing the api
        mUser = SessionsController.getSession().getUser();
        if (mUser != null) {
            if (mUser.getImages() != null) {
                Glide.with(this).load(mUser.getImages().getUrl200_200()).centerCrop().into(userimage);
            }
            loginTxt.setText(mUser.getUsername());
            emailTxt.setText(mUser.getEmail());
            fullNameTxt.setText(mUser.getName());
        }

    }


    private void doSave() {


        if (SessionsController.getSession() != null) {

            //Make sure that the user is connected before executing the api
            mUser = SessionsController.getSession().getUser();
            if (mUser != null) {

                String oldUsername = mUser.getUsername();
                String idUser = String.valueOf(mUser.getId());


                //Display Popup : Loading
                mPdialog = new ProgressDialog(getActivity());
                mPdialog.setMessage("Loading ...");
                mPdialog.setCancelable(false);
                mPdialog.show();


                SimpleRequest request = new SimpleRequest(Request.Method.POST,
                        Constances.API.API_UPDATE_ACCOUNT, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        mPdialog.dismiss();

                        try {

                            if (APP_DEBUG) {
                                Log.i("response", response);
                            }

                            JSONObject js = new JSONObject(response);

                            UserParser mUserParser = new UserParser(js);

                            int success = Integer.parseInt(mUserParser.getStringAttr(Tags.SUCCESS));

                            if (success == 1) {

                                final List<User> list = mUserParser.getUser();
                                if (list.size() > 0) {

                                    if (imageToUpload != null)
                                        CommunFunctions.uploadImage(list.get(0).getId(), getActivity().getApplicationContext(), imageToUpload);

                                    if (APP_DEBUG)
                                        Log.i("__", "logged " + list.get(0).getUsername());

                                    SessionsController.logOut();
                                    (new Handler()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            SessionsController.createSession(list.get(0));
                                            Toast.makeText(getActivity(), "Profile detail has successfully updated", Toast.LENGTH_LONG).show();
                                        }
                                    }, 2000);


                                }


                            } else {


                                Map<String, String> errors = mUserParser.getErrors();


                                MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        MessageDialog.getInstance().hide();
                                    }
                                }).onOkClick(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        MessageDialog.getInstance().hide();
                                    }
                                }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();

                            }


                        } catch (JSONException e) {
                            e.printStackTrace();

                            Map<String, String> errors = new HashMap<String, String>();
                            errors.put("JSONException:", "Try later \"Json parser\"");

                            MessageDialog.newDialog(getActivity()).onCancelClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MessageDialog.getInstance().hide();
                                }
                            }).onOkClick(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    MessageDialog.getInstance().hide();
                                }
                            }).setContent(Translator.print(CommunFunctions.convertMessages(errors), "Message showError")).show();


                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (APP_DEBUG) {
                            Log.e("ERROR", error.toString());
                        }

                        mPdialog.dismiss();
                        Map<String, String> errors = new HashMap<String, String>();

                        errors.put("NetworkException:", getString(R.string.check_nework));
                        mDialogError = CommunFunctions.showErrors(errors, getContext());
                        mDialogError.setTitle(R.string.network_error);

                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();

                        //params.put("password", password.getText().toString().trim());
                        params.put("username", loginTxt.getText().toString().trim());
                        params.put("email", emailTxt.getText().toString().trim());
                        params.put("oldUsername", oldUsername);
                        params.put("name", fullNameTxt.getText().toString());
                        //params.put("phone", codeCountryString + "-" + phone.getText().toString().trim());
                        params.put("user_id", idUser);
                        params.put("image", loadedImageId);
                        params.put("lat", String.valueOf(gps.getLatitude()));
                        params.put("lng", String.valueOf(gps.getLongitude()));
                        params.put("token", Utils.getToken(getActivity().getApplicationContext()));
                        params.put("mac_adr", ServiceHandler.getMacAddr());
                        params.put("auth_type", "mobile");


                        if (APP_DEBUG) {
                            Log.e("EditProfile", " params :" + params.toString());
                        }


                        return params;

                    }

                };


                request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(request);

            }

        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST) {

            try {

                if (resultCode == Activity.RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        cursor.close();

                        String createNewFileDest = CommunFunctions.createImageFile(getActivity());
                        new ImageUtils.PrepareImagesData(
                                getActivity(),
                                picturePath,
                                bitmap,
                                createNewFileDest,
                                this).execute();

                        userimage.setImageBitmap(bitmap);
                    } catch (IOException e) {

                        if (AppContext.DEBUG)
                            e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getActivity(), "You haven't picked Image", Toast.LENGTH_LONG).show();
                }


            } catch (Exception e) {
                if (AppContext.DEBUG)
                    e.printStackTrace();
            }


        }
    }


    @Override
    public void onCompressed(String newPath, String oldPath) {

        File mFile = new File(newPath);

        Glide.with(this).load(mFile).centerCrop()
                .placeholder(R.drawable.profile_placeholder).into(userimage);

        imageToUpload = Uri.parse(newPath);
    }


   /* private void setup_background_img(View rootview) {

        Random Dice = new Random();
        int n = Dice.nextInt(header_bg_list.length);

        Glide.with(this)
                .load(AppHelper.loadDrawable(getActivity(), header_bg_list[n]))
                .centerCrop().into(authBgImg);

    }*/
}