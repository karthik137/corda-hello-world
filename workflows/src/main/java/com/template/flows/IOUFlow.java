package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.TemplateContract;
import com.template.states.IOUState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

@StartableByRPC
@InitiatingFlow
public class IOUFlow extends FlowLogic<Void> {

    private final Integer iouValue;
    private final Party otherParty;

    /**
     * The progress tracker provides checkpoints indicating the progress of
     the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker(){
        return progressTracker;
    }

    public IOUFlow(Integer iouValue, Party otherParty) {
        this.iouValue = iouValue;
        this.otherParty = otherParty;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        //Retrieve the notary identity from the network map;

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        //We create the transaction components;
        IOUState outputState = new IOUState(iouValue, getOurIdentity(), otherParty);
        //Command command = new Command<>(new TemplateContract.Commands.Send())
        Command command = new Command<>(new TemplateContract.Commands.Send(), getOurIdentity().getOwningKey());

        //We create a transaction builder and add the components.
        TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(outputState, TemplateContract.ID)
                .addCommand(command);

        //Signing the transaction;
        SignedTransaction signedTx = getServiceHub().signInitialTransaction(
                txBuilder
        );

        //Create a session with the other party;
        FlowSession otherPartySession = initiateFlow(otherParty);

        //We finalise the transaction and then send it to the counterparty;
        subFlow(new FinalityFlow(signedTx, otherPartySession));

        return null;
    }
}
