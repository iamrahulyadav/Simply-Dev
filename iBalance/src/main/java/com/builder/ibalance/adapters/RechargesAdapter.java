package com.builder.ibalance.adapters;

/*
public class RechargesAdapter extends CursorAdapter{

	public RechargesAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView rechargeDateTextView = (TextView) view.findViewById(R.id.rechargeDate);
		rechargeDateTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
		TextView rechargeAmountTextView = (TextView) view.findViewById(R.id.rechargeAmount);
		rechargeAmountTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
		TextView rechargeBalanceTextView = (TextView) view.findViewById(R.id.rechargeBalance);
		rechargeBalanceTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
		
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d, MMM, yyyy");
		rechargeDateTextView.setText("Date: "+sdf.format(new Date(cursor.getLong(1))));
		rechargeAmountTextView.setText("RechargeAmount: "+ConstantsAndStatics.RUPEE_SYMBOL+" "+String.format("%.0f", cursor.getFloat(2)));
		rechargeBalanceTextView.setText("Balance: "+ConstantsAndStatics.RUPEE_SYMBOL+" "+cursor.getString(3));
		
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		
			View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_recharge, arg2, false);

		*/
/*RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dummyfrag_scrollableview);
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setHasFixedSize(true);*//*

		return view;
	}

}
*/
