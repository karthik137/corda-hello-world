package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.IOUState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;

@InitiatedBy(IOUFlow.class)
public class IOUFlowResponder extends FlowLogic<Void> {
    private final FlowSession otherPartySession;

    public IOUFlowResponder(FlowSession otherPartySession) {
        this.otherPartySession = otherPartySession;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
//        subFlow(new ReceiveFinalityFlow(otherPartySession));
//        return null;

        class SignTxFlow extends  SignTransactionFlow{
            private SignTxFlow(FlowSession otherPartySession){
                super(otherPartySession);
            }


            protected  void checkTransaction(SignedTransaction stx){

                ContractState output = stx.getTx().getOutputs().get(0).getData();
                if(!(output instanceof IOUState)){
                    throw new IllegalArgumentException("State must be of type IOUState");
                }
                IOUState iou = (IOUState)output;
                if(iou.getValue() > 100){
                    throw new IllegalArgumentException("Value cannot be too high");
                }
            }
        }

        SecureHash expectedTxId = subFlow(new SignTxFlow(otherPartySession)).getId();
        subFlow(new ReceiveFinalityFlow(otherPartySession, expectedTxId));
        return null;
    }
}
