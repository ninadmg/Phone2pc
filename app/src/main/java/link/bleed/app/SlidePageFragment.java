package link.bleed.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bleed on 05-02-2015.
 */
public class SlidePageFragment extends Fragment {

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
                R.layout.intro, container, false);
        ImageView imageView = (ImageView) View.findViewById(R.id.mainImage);
        TextView title = (TextView) View.findViewById(R.id.textView2);
        TextView desc = (TextView) View.findViewById(R.id.textView);

        imageView.setImageResource(page.imageResource);
        title.setText(page.title);
        desc.setText(page.desc);
        return View;
    }

    public static SlidePageFragment newInstance(pagerItem page) {
        SlidePageFragment fragmentFirst = new SlidePageFragment();
        Bundle args = new Bundle();
        args.putSerializable("item",page);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }
}