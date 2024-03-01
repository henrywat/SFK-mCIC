package com.sfk.mcic;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.util.List;

public class GalleryImageAdapter extends BaseAdapter {

	private Context context;

	private static ImageView imageView;

	private List<Drawable> plotsImages;

	private static ViewHolder holder;

	public GalleryImageAdapter(Context context, List<Drawable> plotsImages) {

		this.context = context;
		this.plotsImages = plotsImages;

	}

	@Override
	public int getCount() {
		return plotsImages.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			holder = new ViewHolder();

			imageView = new ImageView(this.context);

			imageView.setPadding(3, 3, 3, 3);

			convertView = imageView;

			holder.imageView = imageView;

			convertView.setTag(holder);

		} else {

			holder = (ViewHolder) convertView.getTag();
		}

		holder.imageView.setImageDrawable(plotsImages.get(position));

		holder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		

		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		
		int ddt = 200;
		holder.imageView.setLayoutParams(new Gallery.LayoutParams(size.x-ddt, (size.x-ddt)*3/4));

		return imageView;
	}

	private static class ViewHolder {
		ImageView imageView;
	}

}
