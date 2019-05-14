package prashantkumar.com.getactive;

/**
 * Created by Prashant on 14-05-2019.
 */

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {


    public List<BlogPost> blog_list;
    public Context context;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    private ImageView blog_comment_icon;
    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);

        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        final String blogPostId = blog_list.get(position).BlogPostId;

        final String currentUserId = firebaseAuth.getCurrentUser().getUid();

        String descData = blog_list.get(position).getDesc();
        holder.setDescText(descData);

        String image_url = blog_list.get(position).getImage_url();
        //String thumbUri = blog_list.get(position).getImage_thumb();
        holder.setBlogImage(image_url);


        String user_id = blog_list.get(position).getUser_id();

        //Now we need run a query to get user name
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    holder.setUserData(userName,userImage);

                }else{

                }
            }
        });
        try {
            long millisecond = blog_list.get(position).getTimestamp().getTime();
            String dateString = android.text.format.DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        }catch (Exception e){
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        //Get Likes

        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
               if(documentSnapshot!=null) {
                   if (documentSnapshot.exists()) {
                       holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                   } else {
                       holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_gray));
                   }
               }




            }
        });


        //Get Like count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    if (!documentSnapshots.isEmpty()) {

                        int count = documentSnapshots.size();
                        holder.updateLikeCount(count);
                    } else {
                        holder.updateLikeCount(0);
                        Log.e("My Tag", "Furebase Exception", e);
                    }
                }
            }
        });



        //like Feature
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists()){
                            Map<String , Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).set(likesMap);

                        }else {
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(currentUserId).delete();

                        }
                    }
                });





            }
        });





    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        View mView;
        private TextView descView;
        private ImageView blogImageView;
        private TextView blogdate;
        private  TextView blogUserName;
        private CircleImageView blogUserImage;

        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
        }



        public void setDescText(String descText){
            descView = mView.findViewById(R.id.blog_description);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri){

            if(downloadUri!=null) {
                blogImageView = mView.findViewById(R.id.blog_image);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.background_image);

                Glide.with(context).applyDefaultRequestOptions(requestOptions)
                        .load(downloadUri)
                        .into(blogImageView);
            }else
            {

            }


        }



        public void setTime(String date){
            blogdate = mView.findViewById(R.id.blog_date);
            blogdate.setText(date);
        }

        public void setUserData(String name,String image){
            blogUserName = mView.findViewById(R.id.blog_username);
            blogUserImage = mView.findViewById(R.id.blog_user_image);
            blogUserName.setText(name);
            RequestOptions placeholderOption = new RequestOptions();
            placeholderOption.placeholder(R.drawable.default_image);
            Glide.with(context).applyDefaultRequestOptions(placeholderOption).load(image).into(blogUserImage);

        }


        public void updateLikeCount(int count){

            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");
        }




    }
}
