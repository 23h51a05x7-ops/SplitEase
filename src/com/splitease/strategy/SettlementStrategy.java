
package com.splitease.strategy;

import com.splitease.model.Member;
import com.splitease.model.Settlement;

import java.util.List;
import java.util.Map;

public interface SettlementStrategy {

    List<Settlement> calculateSettlements(
        Map<Member, Double> balances
    );
}
