package com.template.contracts;

import com.template.states.IOUState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractsDSL;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

public class IOUContract implements Contract {

    public static final String ID = "com.template.contracts.IOUContract";

    //Command
    public static class Create implements CommandData{

    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        //Get command
        final CommandWithParties<IOUContract.Create> command = ContractsDSL.requireSingleCommand(
                tx.getCommands(), IOUContract.Create.class
        );

        //constraints on the shape of the transaction.
        if(!tx.getInputs().isEmpty()){
            throw new IllegalArgumentException("No inputs should be consumed when creating IOU");
        }

        if(!(tx.getOutputs().size() == 1)){
            throw new IllegalArgumentException("THere thhould be one output state");
        }

        // IOU-specific constraints.
        final IOUState output = tx.outputsOfType(IOUState.class).get(0);
        final Party lender = output.getLender();
        final Party borrower = output.getBorrower();

        if(output.getValue() <=0)
            throw new IllegalArgumentException("The IOU's value should be non negative");

        if(lender.equals(borrower)){
            throw new IllegalArgumentException("The lender and borrower cannot be same");
        }

        //Constraints on the signers
        final List<PublicKey> requiredSigners = command.getSigners();
        final List<PublicKey> expectedSigners = Arrays.asList(borrower.getOwningKey(), lender.getOwningKey());

        if(requiredSigners.size() != 2)
            throw new IllegalArgumentException("THere must be two signers");

        if(!(requiredSigners.containsAll(expectedSigners))){
            throw new IllegalArgumentException("Borrower and lender must be signers");
        }
    }
}
