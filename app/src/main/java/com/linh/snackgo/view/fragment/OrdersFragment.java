package com.linh.snackgo.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.linh.snackgo.R;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.IntentKeys;
import com.linh.snackgo.controller.OrderController;
import com.linh.snackgo.databinding.FragmentOrdersBinding;
import com.linh.snackgo.model.Order;
import com.linh.snackgo.model.OrderSummary;
import com.linh.snackgo.utils.SessionManager;
import com.linh.snackgo.view.activity.MainActivity;
import com.linh.snackgo.view.activity.OrderDetailActivity;
import com.linh.snackgo.view.adapter.OrderAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Orders tab fragment displaying filterable list of user's orders sorted newest first.
 */
public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private FragmentOrdersBinding binding;
    private OrderController orderController;
    private SessionManager sessionManager;
    private OrderAdapter adapter;
    private String selectedStatusFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        orderController = new OrderController(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupRecyclerView();
        setupFilterChips();
        setupListeners();
        loadOrders();
    }

    private void setupRecyclerView() {
        adapter = new OrderAdapter(this);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);
    }

    private void setupFilterChips() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                selectedStatusFilter = AppConstants.ORDER_STATUS_PENDING;
            } else if (checkedIds.contains(R.id.chipDelivering)) {
                selectedStatusFilter = AppConstants.ORDER_STATUS_DELIVERING;
            } else if (checkedIds.contains(R.id.chipCompleted)) {
                selectedStatusFilter = AppConstants.ORDER_STATUS_COMPLETED;
            } else if (checkedIds.contains(R.id.chipCancelled)) {
                selectedStatusFilter = AppConstants.ORDER_STATUS_CANCELLED;
            } else {
                selectedStatusFilter = "ALL";
            }
            updateChipStyles(checkedIds);
            loadOrders();
        });
    }

    private void updateChipStyles(List<Integer> checkedIds) {
        int activeText = requireContext().getColor(R.color.surface);
        int inactiveText = requireContext().getColor(R.color.primary_ink);

        binding.chipAllOrders.setChipBackgroundColorResource(checkedIds.contains(R.id.chipAllOrders) ? R.color.primary_ink : R.color.surface);
        binding.chipAllOrders.setTextColor(checkedIds.contains(R.id.chipAllOrders) ? activeText : inactiveText);

        binding.chipPending.setChipBackgroundColorResource(checkedIds.contains(R.id.chipPending) ? R.color.primary_ink : R.color.surface);
        binding.chipPending.setTextColor(checkedIds.contains(R.id.chipPending) ? activeText : inactiveText);

        binding.chipDelivering.setChipBackgroundColorResource(checkedIds.contains(R.id.chipDelivering) ? R.color.primary_ink : R.color.surface);
        binding.chipDelivering.setTextColor(checkedIds.contains(R.id.chipDelivering) ? activeText : inactiveText);

        binding.chipCompleted.setChipBackgroundColorResource(checkedIds.contains(R.id.chipCompleted) ? R.color.primary_ink : R.color.surface);
        binding.chipCompleted.setTextColor(checkedIds.contains(R.id.chipCompleted) ? activeText : inactiveText);

        binding.chipCancelled.setChipBackgroundColorResource(checkedIds.contains(R.id.chipCancelled) ? R.color.primary_ink : R.color.surface);
        binding.chipCancelled.setTextColor(checkedIds.contains(R.id.chipCancelled) ? activeText : inactiveText);
    }

    private void setupListeners() {
        binding.btnShopNow.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToTab(R.id.nav_home);
            }
        });
    }

    private void loadOrders() {
        long userId = sessionManager.getUserId();
        List<OrderSummary> allSummaries = orderController.getOrderSummaries(userId);

        List<OrderSummary> filtered = new ArrayList<>();
        if (allSummaries != null) {
            for (OrderSummary s : allSummaries) {
                if (s != null && s.getOrder() != null) {
                    if ("ALL".equalsIgnoreCase(selectedStatusFilter) || selectedStatusFilter.equalsIgnoreCase(s.getOrder().getStatus())) {
                        filtered.add(s);
                    }
                }
            }
        }

        adapter.submitList(new ArrayList<>(filtered));

        if (filtered.isEmpty()) {
            binding.layoutEmptyOrders.setVisibility(View.VISIBLE);
            binding.rvOrders.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyOrders.setVisibility(View.GONE);
            binding.rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        if (order != null) {
            Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
            intent.putExtra(IntentKeys.EXTRA_ORDER_ID, order.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
