package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService{

    private final AuthUtil authUtil;
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public AddressServiceImpl(AuthUtil authUtil, ModelMapper modelMapper, AddressRepository addressRepository, UserRepository userRepository) {
        this.authUtil = authUtil;
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {

        Address address = modelMapper.map(addressDTO,Address.class);

        List<Address> userAddress = user.getAddresses();

        userAddress.add(address);

        address.setUser(user);

        Address savedAddress = addressRepository.save(address);

        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAllAddress() {
        List<Address> allAddress = addressRepository.findAll();
        if(allAddress.isEmpty()){
            throw new APIException("No address found!");
        }
        List<AddressDTO> allAddressDTO = allAddress.stream()
                .map(
                        address -> modelMapper.map(address,AddressDTO.class)
                ).toList();
        return allAddressDTO;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Address","addressId",addressId)
                );

        return modelMapper.map(address,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddressByUser() {
        User user = authUtil.loggedInUser();

        List<Address> userAddress  = addressRepository.findByUser(user);

        if(userAddress.isEmpty())
        {
            throw new APIException("No address associated to the user "+user.getUserName());
        }
        List<AddressDTO> userAddressDTOs = userAddress.stream()
                .map(
                        address -> modelMapper.map(address,AddressDTO.class)
                ).toList();


        return userAddressDTOs;
    }

    @Override
    public AddressDTO updateAddressById(Long addressId,AddressDTO addressDTO) {
        Address addressFromDB = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address","addressId",addressId)
                );

        addressFromDB.setCity(addressDTO.getCity());
        addressFromDB.setPincode(addressDTO.getPincode());
        addressFromDB.setCountry(addressDTO.getCountry());
        addressFromDB.setState(addressDTO.getState());
        addressFromDB.setBuildingName(addressDTO.getBuildingName());
        addressFromDB.setStreet(addressDTO.getStreet());

        Address updatedAddress = addressRepository.save(addressFromDB);
        // even tough the owing side is address in the DB we update the user object
        // because the java in memory does not automatically update when DB is updated
        // we have to keep both of them in sync otherwise
        // after the update if someone saves user again since in java in memory is not updated or deleted
        // the address might be persisted into DB again with incorrect values.
        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(address ->
                address.getAddressId().equals(addressFromDB.getAddressId()));
        user.getAddresses().add(updatedAddress);

        userRepository.save(user);

        return modelMapper.map(updatedAddress,AddressDTO.class);
    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address addressFromDB = addressRepository.findById(addressId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Address","addressId",addressId)
                );
        addressRepository.deleteById(addressId);

        User user = addressFromDB.getUser();
        user.getAddresses().removeIf(
                address -> address.getAddressId().equals(addressFromDB.getAddressId())
        );
        userRepository.save(user);

        return "Delete the address with id : "+addressId;
    }
}
