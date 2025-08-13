// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract PolicyVotingContract {

    struct Policy {
        uint256 id;
        string organType;
        string description;
        string rules; // JSON string
        address proposer;
        Status status;
        uint256 votes;
    }

    enum Status { PENDING, ACTIVE, EXPIRED }

    Policy[] public policies;
    uint256 public policyCount = 0;

    mapping(uint256 => mapping(address => bool)) public votes;

    event PolicyProposed(uint256 id, address proposer);
    event PolicyVoted(uint256 id, address voter);
    event PolicyActivated(uint256 id);

    function proposePolicy(string memory _organType, string memory _description, string memory _rules) public {
        policyCount++;
        policies.push(Policy(policyCount, _organType, _description, _rules, msg.sender, Status.PENDING, 0));
        emit PolicyProposed(policyCount, msg.sender);
    }

    function vote(uint256 _policyId) public {
        require(_policyId > 0 && _policyId <= policyCount, "Invalid policy ID");
        require(!votes[_policyId][msg.sender], "You have already voted on this policy");

        Policy storage policy = policies[_policyId - 1];
        require(policy.status == Status.PENDING, "Voting for this policy is closed");

        policy.votes++;
        votes[_policyId][msg.sender] = true;

        // For simplicity, a policy becomes active after a certain number of votes (e.g., 5).
        // In a real-world scenario, you would have a more complex voting mechanism.
        if (policy.votes >= 5) {
            policy.status = Status.ACTIVE;
            emit PolicyActivated(_policyId);
        }

        emit PolicyVoted(_policyId, msg.sender);
    }

    function getPolicy(uint256 _policyId) public view returns (uint256, string memory, string memory, string memory, address, Status, uint256) {
        require(_policyId > 0 && _policyId <= policyCount, "Invalid policy ID");
        Policy storage policy = policies[_policyId - 1];
        return (policy.id, policy.organType, policy.description, policy.rules, policy.proposer, policy.status, policy.votes);
    }
}
