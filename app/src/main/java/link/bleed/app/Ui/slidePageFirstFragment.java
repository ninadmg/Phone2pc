package link.bleed.app.Ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import link.bleed.app.Models.pagerItem;
import link.bleed.app.R;

/**
 * Created by bleed on 05-02-2015.
 */
public class slidePageFirstFragment extends Fragment {

    pagerItem page;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = (pagerItem) getArguments().getSerializable("item");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup View = (ViewGroup) inflater.inflate(
                R.layout.into_first, container, false);
        ImageView imageView = (ImageView) View.findViewById(R.id.mainImage);
        TextView title = (TextView) View.findViewById(R.id.textView2);
        TextView desc = (TextView) View.findViewById(R.id.textView);

        imageView.setImageResource(page.imageResource);
        title.setText(page.title);
        desc.setText(page.desc);
        return View;
    }

    public static slidePageFirstFragment newInstance(pagerItem page) {
        slidePageFirstFragment fragmentFirst = new slidePageFirstFragment();
        Bundle args = new Bundle();
        args.putSerializable("item", page);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }
}